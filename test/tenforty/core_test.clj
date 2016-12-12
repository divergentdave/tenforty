(ns tenforty.core-test
  (:require [clojure.test :refer :all]
            [tenforty.core :refer :all]))

(deftest data-deps
  (testing "Get data dependencies from a quoted form"
    (is (= (data-dependencies '(cell-value :AGI)) '(:AGI)))
    (is (= (data-dependencies '(+ 1 1)) '()))
    (is (= (data-dependencies '(identity 0)) '()))
    (is (= (data-dependencies '(+ (cell-value :AGI) 1)) '(:AGI)))
    (is (= (data-dependencies '(- (cell-value :pretotal_tax) (cell-value :total_credits))) '(:pretotal_tax :total_credits)))))

(deftest makeline-no-deps
  (testing "Macro to create a line with a degenerate expression"
    (let [myline
          (makeline ::myline 0)]
      (is (= (:kw myline) ::myline))
      (is (fn? (:fn myline)))
      (is (= (:deps myline) ()))
      (is (== ((:fn myline) (fn [kw] 1234)) 0)))))

(deftest makeline-test
  (testing "Macro to create a line with an expression"
    (let [tax_minus_credits
          (makeline :tenforty.forms.ty2015.f1040/tax_minus_credits (max (- (cell-value :tenforty.forms.ty2015.f1040/pretotal_tax) (cell-value :tenforty.forms.ty2015.f1040/total_credits) (cell-value :tenforty.forms.ty2015.s8812/ctc)) 0))]
      (is (= (:kw tax_minus_credits) :tenforty.forms.ty2015.f1040/tax_minus_credits))
      (is (fn? (:fn tax_minus_credits)))
      (is (= (:deps tax_minus_credits) (list :tenforty.forms.ty2015.f1040/pretotal_tax :tenforty.forms.ty2015.f1040/total_credits :tenforty.forms.ty2015.s8812/ctc)))
      (is (== ((:fn tax_minus_credits) (fn [kw] 0)) 0))
      (is (== ((:fn tax_minus_credits) (fn [kw] 1)) 0))
      (is (== ((:fn tax_minus_credits) (fn [kw] -1)) 1)))))
