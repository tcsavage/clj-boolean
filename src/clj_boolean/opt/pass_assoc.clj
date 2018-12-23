(ns clj-boolean.opt.pass-assoc
  "Leverages the associative property to remove nested ANDs and ORs."
  (:require [clj-boolean.syntax :as syn]))

(defmulti ^{:pass-name "assoc"} pass syn/node-tag)

(defmethod pass :default [node] node)

(defmethod pass :clj-boolean.syntax/n-ary-operator
  [node]
  (let [tag (syn/node-tag node)
        children (:children node)
        groups (group-by syn/node-tag children)
        same-op (get groups tag)
        others (apply concat (vals (dissoc groups tag)))]
    (assoc node :children (apply concat others (map :children same-op)))))
