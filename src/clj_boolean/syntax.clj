(ns clj-boolean.syntax
  "Core syntax nodes and operations."
  (:require [clojure.core :as cc]
            [clojure.walk :as walk])
  (:refer-clojure :exclude [and or not]))

(defn node
  "Constructs a new node object."
  [tag & {:as props}]
  (assoc props ::tag tag))

(defn n-ary-operator
  "Helper constructor for building nodes representing n-ary operators.
  Nil operands are removed.
  It's possible to create a node with zero children."
  [tag & operands]
  (node tag :children (remove nil? operands)))

(def ^{:arglists '([& children])
       :doc "Creates a new AND node with the given children."}
  and
  (partial n-ary-operator ::and))
(derive ::and ::n-ary-operator)
(derive ::and ::builtin)

(def ^{:arglists '([& children])
       :doc "Creates a new OR node with the given children."}
  or
  (partial n-ary-operator ::or))
(derive ::or ::n-ary-operator)
(derive ::or ::builtin)

(defn not
  "Creates a new NOT node with the given child."
  [operand]
  (node ::not :child operand))
(derive ::not ::builtin)

(defn node?
  "Returns true if the input is a valid node object."
  [node]
  (cc/and
   (map? node)
   (::tag node)
   (keyword? (::tag node))))

(defn node-tag
  "Extracts the tag component of a node object."
  [node]
  (let [tag (::tag node)]
    tag))

(defn walk-query
  "Performs a depth-first, post-order traversal of the given query, applying
  alg to every node encountered.
  alg must be a function which takes a node with any children replaced by
  whatever alg returns."
  [alg query]
  (letfn [(walker [form] (if (node? form) (alg form) form))]
    (walk/postwalk walker query)))

(defmulti normalise-node
  "Produces a node with the same semantic meaning but with a standard structure
  for better equality comparisons."
  node-tag)
(defmethod normalise-node :default [node] node)
(defmethod normalise-node ::n-ary-operator
  [node]
  (update node :children #(sort-by hash %)))

(defn normalise-query
  [query]
  (walk-query normalise-node query))

(defn- with-select-children-dispatch
  [node & _]
  (node-tag node))

(defmulti with-select-children
  "Takes a node, a selector predicate, and a function, extracts all children
  which match the selector predicate, passes them to f, and merges the result
  back into the remaining children.
  selector should take a node and return true or false.
  f should take a collection of nodes and any values in args.
  f should return either another list of nodes or a single node.
  If no children are selected by pred, the node is returned unchanged.
  If all children are selected, the result of f is returned.
  If f returns a collection in this case it will be wrapped in the original node."
  {:arglists '([node selector f & args])}
  with-select-children-dispatch)

(defmethod with-select-children :default [node & _] node)

(defmethod with-select-children ::not
  [node selector f & args]
  (let [{child :child} node]
    (if (selector child)
      (let [f-result (apply f node args)]
        (if (node? f-result)
          (assoc node :child f-result)
          (assoc node :child (apply or f-result))))
      node)))

(defmethod with-select-children ::n-ary-operator
  [node selector f & args]
  ;; Try to extract distributor nodes from children...
  (let [{children :children} node
        child-groups (group-by (comp boolean selector) children)]
    (if-let [selected-nodes (get child-groups true)]
      ;; We have selected some nodes, so apply f...
      (let [f-result (apply f selected-nodes args)
            other-nodes (get child-groups false)]
        (if (seq other-nodes)
          ;; We have other child nodes to merge with...
          ;; If f-result is a node, we wrap it in a vector so we can concat.
          ;; If it's not a node, assume we can just concat as-is.
          (let [new-children (concat (if (node? f-result) [f-result] f-result) other-nodes)]
            ;; Return a new node with all the children merged together.
            (assoc node :children new-children))
          ;; We only have to return the result...
          ;; If f-result is a node, just return that.
          ;; If it's not a node, assume it's a collection of nodes and use them
          ;; as the new children of the parent.
          (if (node? f-result)
            f-result
            (assoc node :children f-result))))
      ;; We don't have any selected nodes, so return node unchanged...
      node)))