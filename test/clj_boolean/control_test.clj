(ns clj-boolean.control-test
  (:require [clj-boolean.control :as control]
            [clojure.test :refer :all]))

(defn factorial*
  [tracker n]
  (control/cycle
   tracker n
   (if (zero? n)
     1
     (* n (factorial* tracker (dec n))))))

(defn factorial
  [n]
  (factorial* (control/cycle-detector) n))

(defn broken*
  [tracker n]
  (control/cycle
   tracker n
   (if (zero? n)
     1
     (* n (broken* tracker n)))))

(defn broken
  [n]
  (broken* (control/cycle-detector) n))

(deftest test-cycle-detector
  (testing "Factorial"
    (is (= 120 (factorial 5))))
  (testing "Broken fectorial"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Cycle detected" (broken 5)))))