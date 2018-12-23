(ns clj-boolean.analysis
  "Static analysis on queries."
  (:require [clj-boolean.syntax :as syn]))

;; Counting stats.

(defmulti node-stats
  "Produce a map of stats for the node. The default implementation returns {TAG 1 :total 1}."
  syn/node-tag)

(defn- singleton
  [tag]
  {tag 1 :total 1})

(defmethod node-stats :default
  [node]
  (singleton (syn/node-tag node)))

(defn query-stats
  "Walk query collecting stats via the node-stats multimethod.
  Any additional keys inserted into stats map will be preserved by built-in nodes."
  [query]
  (syn/walk-query node-stats query))

(defmethod node-stats :clj-boolean.syntax/n-ary-operator
  [{children :children :as node}]
  (apply merge-with + (singleton (syn/node-tag node)) children))

(defmethod node-stats :clj-boolean.syntax/not
  [{child :child}]
  (merge-with + (singleton :clj-boolean.syntax/not) child))

;; Query weight

(defmulti node-weight
  "Calculate the weight of a given node. The default implementation is 1."
  syn/node-tag)

(defmethod node-weight :default [node] 1)

;; Assume the weight of an AND or an OR is just the sum of all children.
(defmethod node-weight :clj-boolean.syntax/n-ary-operator
  [{children :children}]
  (apply + children))

;; Assume the weight of a NOT is that of its child.
(defmethod node-weight :clj-boolean.syntax/not
  [{child :child}]
  child)

(defn query-weight
  "Walk the query calculating the total cumulative weight. Extend for custom
  nodes with the node-weight multimethod."
  [query]
  (syn/walk-query node-weight query))