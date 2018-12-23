(ns clj-boolean.test-generators
  "This namespace defines and implements multimethods for a single generator
  family for test purposes."
  (:require [clj-boolean.pretty :as pretty]
            [clj-boolean.syntax :as syn]))

(defn phrase
  "Constructor for phrase nodes."
  [text]
  (syn/node ::phrase :phrase text))

;;; Pretty printer implementation.
(defmethod pretty/pp-node ::phrase
  [{phrase :phrase}]
  (pr-str phrase))
