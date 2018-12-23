(ns clj-boolean.opt.pass-singletons-test
  (:require [clj-boolean.syntax :as syn]
            [clj-boolean.opt.pass-singletons :refer :all]
            [clj-boolean.test-generators :as Generator]
            [clojure.test :refer :all]))

(deftest test-pass
  (are [in ex] (= (syn/normalise-query ex) (->> in (syn/walk-query pass) syn/normalise-query))
       ;; Generator only.
    (Generator/phrase "p1")
    (Generator/phrase "p1")
       ;; Singleton.
    (syn/and (Generator/phrase "p1"))
    (Generator/phrase "p1")
       ;; Non-singleton.
    (syn/and (Generator/phrase "p1") (Generator/phrase "p2"))
    (syn/and (Generator/phrase "p1") (Generator/phrase "p2"))))
