(ns tenforty.core-test
  (:require [clojure.test :refer :all]
            [tenforty.core :refer :all]))

(deftest data-deps-1
  (testing "Get data dependencies from a quoted form"
    (is (= (data-dependencies '(cell-value :AGI)) '(:AGI)))
    ))

(deftest data-deps-2
  (testing "Get data dependencies from a quoted form"
    (is (= (data-dependencies '(+ 1 1)) '()))
    ))

(deftest data-deps-3
  (testing "Get data dependencies from a quoted form"
    (is (= (data-dependencies '(identity 0)) '()))
    ))

(deftest data-deps-4
  (testing "Get data dependencies from a quoted form"
    (is (= (data-dependencies '(+ (cell-value :AGI) 1)) '(:AGI)))
    ))

(deftest data-deps-5
  (testing "Get data dependencies from a quoted form"
    (is (= (data-dependencies '(- (cell-value :pretotal_tax) (cell-value :total_credits))) '(:pretotal_tax :total_credits)))
    ))
