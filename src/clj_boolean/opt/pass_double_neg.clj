(ns clj-boolean.opt.pass-double-neg
  "Eliminates a NOT nested inside another NOT."
  (:require [clj-boolean.syntax :as syn]))

(defmulti ^{:pass-name "double neg"} pass syn/node-tag)

(defmethod pass :default [node] node)

(defmethod pass :clj-boolean.syntax/not
  [{child :child :as node}]
  ;; If the child of this not node is also a not...
  (if (= :clj-boolean.syntax/not (syn/node-tag child))
    ;; Return that child's child.
    (:child child)
    ;; Otherwise, do nothing.
    node))
