(ns clj-boolean.control
  "Helpers to do with control, particularly detecting cycles in recursive
  function calls (for debugging.)"
  (:refer-clojure :exclude [cycle]))

;; Cycle detection helpers.

(defn cycle-detector
  "Create a new object representing the state of a cycle detector."
  []
  (atom #{}))

(defmacro cycle
  "Register a new cycle in state.
  If state has already seen the value of tag, raises an exception."
  [state tag & body]
  `(do
     (when (contains? (deref ~state) ~tag)
       (throw (ex-info "Cycle detected" {:tag ~tag})))
     (swap! ~state conj ~tag)
     ~@body))