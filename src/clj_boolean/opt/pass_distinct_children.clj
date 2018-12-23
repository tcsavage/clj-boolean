(ns clj-boolean.opt.pass-distinct-children
  "Remove duplicated children in n-ary operators."
  (:require [clj-boolean.syntax :as syn]))

(defmulti ^{:pass-name "distinct children"} pass syn/node-tag)

(defmethod pass :default [node] node)

(defmethod pass :clj-boolean.syntax/n-ary-operator
  [node]
  (update node :children (comp distinct (partial map syn/normalise-query))))
