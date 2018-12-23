(ns clj-boolean.syntax-test
  (:require [clj-boolean.syntax :as syn]
            [clj-boolean.test-generators :as gen]
            [clojure.test :refer :all]))

(def ex1
  (syn/and
   (syn/or
    (gen/phrase "foo")
    (gen/phrase "bar"))
   (syn/or
    (gen/phrase "baz")
    (gen/phrase "qux"))
   (gen/phrase "flob")))

(deftest test-with-select-children-n-ary
  (testing "None match"
    (let [result (syn/with-select-children
                   ex1
                   (constantly false)
                   (constantly (gen/phrase "flarp")))]
      (is (= result ex1))))
  (testing "All match, return node"
    (let [result (syn/with-select-children
                   ex1
                   (constantly true)
                   (constantly (gen/phrase "flarp")))
          expected (gen/phrase "flarp")]
      (is (= result expected))))
  (testing "All match, return collection"
    (let [result (syn/with-select-children
                   ex1
                   (constantly true)
                   (constantly [(gen/phrase "flarp") (gen/phrase "frood")]))
          expected (syn/and
                    (gen/phrase "flarp")
                    (gen/phrase "frood"))]
      (is (= result expected))))
  (testing "Some match, return node"
    (let [result (syn/with-select-children
                   ex1
                   (comp #{:clj-boolean.syntax/or} syn/node-tag)
                   (constantly (gen/phrase "flarp")))
          expected (syn/and
                    (gen/phrase "flarp")
                    (gen/phrase "flob"))]
      (is (= result expected))))
(testing "Some match, return collection"
  (let [result (syn/with-select-children
                 ex1
                 (comp #{:clj-boolean.syntax/or} syn/node-tag)
                 (constantly [(gen/phrase "flarp") (gen/phrase "frood")]))
        expected (syn/and
                  (gen/phrase "flarp")
                  (gen/phrase "frood")
                  (gen/phrase "flob"))]
    (is (= result expected)))))