(ns clj-boolean.pretty
  "Query pretty printer."
  (:require [clj-boolean.syntax :as syn]
            [clojure.string :as string]))

(def ^:dynamic *child-limit* 10)

(defmulti pp-node
  "Pretty-prints a node.
  All child nodes should already be pretty-printed strings."
  syn/node-tag)

(defn pp
  "Pretty-print a query. Extend for custom nodes using the pp-node multimethod."
  [query]
  (syn/walk-query pp-node query))

(defn- limit
  [terms]
  (if (> (count terms) *child-limit*)
    (concat (take *child-limit* terms) ["..."])
    terms))

(defmethod pp-node :clj-boolean.syntax/and
  [{terms :children}]
  (format "(%s)" (string/join " ∧ " (limit terms))))

(defmethod pp-node :clj-boolean.syntax/or
  [{terms :children}]
  (format "(%s)" (string/join " ∨ " (limit terms))))

(defmethod pp-node :clj-boolean.syntax/not
  [{term :child}]
  (format "(¬ %s)" term))

(defmethod pp-node :default
  [node]
  (throw (ex-info (format "No implementation of pp-node defined for %s nodes" (syn/node-tag node)) {:node node})))