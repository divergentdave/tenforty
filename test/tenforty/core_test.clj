(ns tenforty.core-test
  (:require [clojure.test :refer :all]
            [tenforty.core :refer :all]))

(deftest data-deps
  (testing "Get data dependencies from a quoted form"
    (is (= (data-dependencies '(cell-value :AGI)) #{:AGI}))
    (is (= (data-dependencies '(+ 1 1)) #{}))
    (is (= (data-dependencies '(identity 0)) #{}))
    (is (= (data-dependencies '(+ (cell-value :AGI) 1)) #{:AGI}))
    (is (= (data-dependencies '(- (cell-value :pretotal_tax) (cell-value :total_credits))) #{:pretotal_tax :total_credits}))
    (is (= (data-dependencies '(let [temp (cell-value :AGI)] temp)) #{:AGI}))))

(deftest makeline-no-deps
  (testing "Macro to create a line with a degenerate expression"
    (let [myline
          (makeline ::myline 0)]
      (is (= (:kw myline) ::myline))
      (is (fn? (:fn myline)))
      (is (= (:deps myline) #{}))
      (is (== ((:fn myline) (fn [kw] 1234)) 0)))))

(deftest makeline-test
  (testing "Macro to create a line with an expression"
    (let [tax_minus_credits
          (makeline :tenforty.forms.ty2015.f1040/tax_minus_credits (max (- (cell-value :tenforty.forms.ty2015.f1040/pretotal_tax) (cell-value :tenforty.forms.ty2015.f1040/total_credits) (cell-value :tenforty.forms.ty2015.s8812/ctc)) 0))]
      (is (= (:kw tax_minus_credits) :tenforty.forms.ty2015.f1040/tax_minus_credits))
      (is (fn? (:fn tax_minus_credits)))
      (is (= (:deps tax_minus_credits) #{:tenforty.forms.ty2015.f1040/pretotal_tax :tenforty.forms.ty2015.f1040/total_credits :tenforty.forms.ty2015.s8812/ctc}))
      (is (== ((:fn tax_minus_credits) (fn [kw] 0)) 0))
      (is (== ((:fn tax_minus_credits) (fn [kw] 1)) 0))
      (is (== ((:fn tax_minus_credits) (fn [kw] -1)) 1)))))

(deftest defform-test
  (defform)
  (is (= form {}))
  (defform
    (makeline ::refund (max (- (cell-value ::total_payments) (cell-value ::total_tax)) 0)))
  (is (= (:kw (::refund form)) ::refund))
  (is (= (:deps (::refund form)) #{::total_payments ::total_tax})))

(deftest duplicate-line-test
  (is (thrown? IllegalArgumentException
               (defform
                 (->InputLine ::a)
                 (->InputLine ::a)))))

(deftest zero-tax-situation-test
  (is (= 0 (lookup (->ZeroTaxSituation) ::a))))

(deftest map-tax-situation-test
  (let [situation (->MapTaxSituation {::a 10})]
    (is (= 10 (lookup situation ::a)))))

(deftest composite-tax-situation-test
  (let [situation (->CompositeTaxSituation [(->MapTaxSituation {::a 10})
                                            (->MapTaxSituation {::a 20
                                                                ::b 30})])]
    (is (= 10 (lookup situation ::a)))
    (is (= 30 (lookup situation ::b)))))
