(ns clj-boolean.opt.pass-empty-test
  (:require [clj-boolean.syntax :as syn]
            [clj-boolean.opt.pass-empty :refer :all]
            [clj-boolean.test-generators :as gen]
            [clojure.test :refer :all]))

(deftest test-pass
  (are [in ex] (= (syn/normalise-query ex) (->> in (syn/walk-query pass) syn/normalise-query))
       ;; Generator only.
    (gen/phrase "p1")
    (gen/phrase "p1")
       ;; And.
    (syn/and (gen/phrase "p1") (gen/phrase "p2"))
    (syn/and (gen/phrase "p1") (gen/phrase "p2"))
       ;; And with nil.
    (syn/and (gen/phrase "p1") nil (gen/phrase "p2"))
    (syn/and (gen/phrase "p1") (gen/phrase "p2"))
       ;; And with nils only.
    (syn/and nil nil)
    nil
       ;; Not.
    (syn/not (gen/phrase "p1"))
    (syn/not (gen/phrase "p1"))
       ;; Not with nil.
    (syn/not nil)
    nil))
