(ns clj-boolean.opt.pass-singletons
  "Unwraps n-ary operators with a single child, leaving just the child on its own."
  (:require [clj-boolean.syntax :as syn]))

(defmulti ^{:pass-name "singletons"} pass syn/node-tag)

(defmethod pass :default [node] node)

(defmethod pass :clj-boolean.syntax/n-ary-operator
  [{children :children :as node}]
  (if (= 1 (count children))
    (first children)
    node))
