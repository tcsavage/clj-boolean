(ns clj-boolean.opt.core
  "Optimiser framework. Use this rather than invoking passes individually."
  (:require [clj-boolean.opt.pass-assoc :as pass-assoc]
            [clj-boolean.opt.pass-double-neg :as pass-double-neg]
            [clj-boolean.opt.pass-distrib :as pass-distrib]
            [clj-boolean.opt.pass-distinct-children :as pass-distinct-children]
            [clj-boolean.opt.pass-singletons :as pass-singletons]
            [clj-boolean.opt.pass-empty :as pass-empty]
            [clj-boolean.syntax :as syn]
            [clj-boolean.timing :as timing]
            [clojure.tools.logging :as log]))

(def ^:dynamic *max-runs*
  "The maxinum number of times all the passes will be run over the AST."
  5)

(def ^:dynamic *passes*
  "A list of vars pointing to pass functions to run against the AST."
  [#'pass-double-neg/pass
   #'pass-distrib/pass
   #'pass-assoc/pass
   #'pass-distinct-children/pass
   #'pass-singletons/pass
   #'pass-empty/pass])

(defn wrap-query-pass
  "Turns a optimser pass on a node into an optimiser pass on a query.
  Also adds some logging."
  [pass]
  (fn [query]
    (log/infof "Starting pass %s" pass)
    (let [[result time] (timing/time-ret (timing/time-limit (syn/walk-query pass query) 100000 (spit (str "dump_query.edn") (pr-str query))))]
      (log/infof "Pass %s took %.2f msecs." pass time)
      result)))

(defn opt
  "Run all optimiser passes over the query a maximum of *max-runs* times, or until the optimiser
  returns the query unchanged."
  [query]
  (let [pipeline (map wrap-query-pass *passes*)
        opt-fn (apply comp (reverse pipeline))
        runs (repeat *max-runs* opt-fn)
        [query' opt-time] (timing/time-ret
                           (reduce
                            (fn [q [f n]]
                              (log/infof "Running optimiser run %d of %d." (inc n) *max-runs*)
                              (let [q' (f q)]
                                (if (= q q')
                                  (do
                                    (log/info "No change. Dropping out of optimiser early.")
                                    (reduced q'))
                                  q')))
                            query
                            (map vector runs (range))))]
    (log/infof "Finished optimising. Time taken: %.2f msecs." opt-time)
    query'))
