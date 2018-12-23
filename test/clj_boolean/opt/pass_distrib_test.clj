(ns clj-boolean.opt.pass-distrib-test
  (:require [clj-boolean.syntax :as syn]
            [clj-boolean.opt.pass-distrib :refer :all]
            [clj-boolean.pretty :as pretty]
            [clj-boolean.test-generators :as gen]
            [clojure.test :refer :all]))

(deftest test-pass
  (let [gen-1 (gen/phrase "foo")
        gen-2 (gen/phrase "bar")
        gen-3 (gen/phrase "baz")
        gen-4 (gen/phrase "qux")
        gen-5 (gen/phrase "quuz")
        gen-6 (gen/phrase "corge")
        gen-7 (gen/phrase "grault")
        gen-8 (gen/phrase "garply")
        nothing-to-do (syn/or gen-1 gen-2)]
    (are [in ex] (= (syn/normalise-query ex) (->> in (syn/walk-query pass) syn/normalise-query))
      ;; Basic.
      gen-1 gen-1
      nothing-to-do nothing-to-do

      ;; ANDs in ORs
      (syn/or
       (syn/and gen-1 gen-2 gen-3 gen-4)
       (syn/and gen-1 gen-2 gen-5 gen-6))
      (syn/and
       gen-1 gen-2
       (syn/or
        (syn/and gen-3 gen-4)
        (syn/and gen-5 gen-6)))

      ;; ANDs in ORs partial
      (syn/or
       (syn/and gen-1 gen-2 gen-3 gen-4)
       (syn/and gen-7 gen-8)
       (syn/and gen-1 gen-2 gen-5 gen-6))
      (syn/or
       (syn/and gen-7 gen-8)
       (syn/and
        gen-1 gen-2
        (syn/or
         (syn/and gen-3 gen-4)
         (syn/and gen-5 gen-6))))

      ;; ANDs in ORs with other
      (syn/or
       (syn/and gen-1 gen-2 gen-3 gen-4)
       (syn/not gen-7)
       (syn/and gen-1 gen-2 gen-5 gen-6))
      (syn/or
       (syn/not gen-7)
       (syn/and
        gen-1 gen-2
        (syn/or
         (syn/and gen-3 gen-4)
         (syn/and gen-5 gen-6))))


      ;; ANDs in ORs
      (syn/and
       (syn/or gen-1 gen-2 gen-3 gen-4)
       (syn/or gen-1 gen-2 gen-5 gen-6))
      (syn/or
       gen-1 gen-2
       (syn/and
        (syn/or gen-3 gen-4)
        (syn/or gen-5 gen-6)))

      ;; ANDs in ORs partial
      (syn/and
       (syn/or gen-1 gen-2 gen-3 gen-4)
       (syn/or gen-7 gen-8)
       (syn/or gen-1 gen-2 gen-5 gen-6))
      (syn/and
       (syn/or gen-7 gen-8)
       (syn/or
        gen-1 gen-2
        (syn/and
         (syn/or gen-3 gen-4)
         (syn/or gen-5 gen-6))))

      ;; ANDs in ORs with other
      (syn/and
       (syn/or gen-1 gen-2 gen-3 gen-4)
       (syn/not gen-7)
       (syn/or gen-1 gen-2 gen-5 gen-6))
      (syn/and
       (syn/not gen-7)
       (syn/or
        gen-1 gen-2
        (syn/and
         (syn/or gen-3 gen-4)
         (syn/or gen-5 gen-6)))))))
