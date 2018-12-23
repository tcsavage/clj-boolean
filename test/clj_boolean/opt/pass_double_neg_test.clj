(ns clj-boolean.opt.pass-double-neg-test
  (:require [clj-boolean.syntax :as syn]
            [clj-boolean.opt.pass-double-neg :refer :all]
            [clj-boolean.test-generators :as gen]
            [clojure.test :refer :all]))

(deftest test-pass
  (are [in ex] (= (syn/normalise-query ex) (->> in (syn/walk-query pass) syn/normalise-query))
       ;; Generator only.
    (gen/phrase "p1")
    (gen/phrase "p1")
       ;; Not.
    (syn/not (gen/phrase "p1"))
    (syn/not (gen/phrase "p1"))
       ;; Not not.
    (syn/not (syn/not (gen/phrase "p1")))
    (gen/phrase "p1")))
