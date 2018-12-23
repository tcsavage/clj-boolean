(ns clj-boolean.timing
  "Helpers to do with timing.")

(defmacro time-ret
  "Like clojure.core/time but returns a pair of [result time]."
  [expr]
  `(let [start# (. System (nanoTime))
         ret# ~expr]
     [ret# (/ (double (- (. System (nanoTime)) start#)) 1000000.0)]))

(defmacro time-limit
  "Term is evaluated with a timeout."
  [term limit-ms error-form]
  (let [term-str (pr-str term)]
    `(let [f# (future ~term)
           r# (deref f# ~limit-ms ::timeout)]
       (if (= ::timeout r#)
         (do
           ~error-form
           (throw (Exception. (format "Timeout evaulating form %s" ~term-str))))
         r#))))