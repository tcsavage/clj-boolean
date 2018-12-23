(ns clj-boolean.opt.pass-distinct-children-test
  (:require [clj-boolean.syntax :as syn]
            [clj-boolean.opt.pass-distinct-children :refer :all]
            [clj-boolean.test-generators :as gen]
            [clojure.test :refer :all]))

(deftest test-pass
  (are [in ex] (= (syn/normalise-query ex) (->> in (syn/walk-query pass) syn/normalise-query))
       ;; Generator only.
    (gen/phrase "p1")
    (gen/phrase "p1")
       ;; No dupes.
    (syn/and (gen/phrase "p1") (gen/phrase "p2"))
    (syn/and (gen/phrase "p1") (gen/phrase "p2"))
       ;; Dupes.
    (syn/and (gen/phrase "p1") (gen/phrase "p2") (gen/phrase "p1"))
    (syn/and (gen/phrase "p1") (gen/phrase "p2"))))
