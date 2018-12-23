(ns clj-boolean.opt.pass-empty
  "Emiminates nil children and n-ary operators with no children."
  (:require [clj-boolean.syntax :as syn]))

(defmulti ^{:pass-name "empty"} pass syn/node-tag)

(defmethod pass :default [node] node)

(defmethod pass :clj-boolean.syntax/n-ary-operator
  [{children :children :as node}]
  (let [non-nil (remove nil? children)]
    (when (seq non-nil)
      (assoc node :children non-nil))))

(defmethod pass :clj-boolean.syntax/not
  [{child :child :as node}]
  (when child node))
