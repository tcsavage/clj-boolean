(ns clj-boolean.opt.pass-assoc-test
  (:require [clj-boolean.syntax :as syn]
            [clj-boolean.opt.pass-assoc :refer :all]
            [clj-boolean.pretty :as pretty]
            [clj-boolean.test-generators :as gen]
            [clojure.test :refer :all]))

(deftest test-pass
  (let [in (syn/and (gen/phrase "p1")
                    (syn/and (gen/phrase "p2") (gen/phrase "p3") (gen/phrase "p4"))
                    (gen/phrase "p5")
                    (gen/phrase "p6")
                    (syn/or (gen/phrase "p7") (gen/phrase "p8") (gen/phrase "p9")))
        ex (syn/and (gen/phrase "p1")
                    (gen/phrase "p2") (gen/phrase "p3") (gen/phrase "p4")
                    (gen/phrase "p5")
                    (gen/phrase "p6")
                    (syn/or (gen/phrase "p7") (gen/phrase "p8") (gen/phrase "p9")))]
    (is (= (syn/normalise-query ex) (->> in (syn/walk-query pass) syn/normalise-query)))))
