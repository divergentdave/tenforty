(ns tenforty.core-test
  (:require [clojure.test :refer :all]
            [tenforty.core :refer :all]))

(deftest data-deps
  (testing "Get data dependencies from a quoted form"
    (is (= (data-dependencies '(cell-value :AGI)) '(:AGI)))
    (is (= (data-dependencies '(+ 1 1)) '()))
    (is (= (data-dependencies '(identity 0)) '()))
    (is (= (data-dependencies '(+ (cell-value :AGI) 1)) '(:AGI)))
    (is (= (data-dependencies '(- (cell-value :pretotal_tax) (cell-value :total_credits))) '(:pretotal_tax :total_credits)))
    ))
