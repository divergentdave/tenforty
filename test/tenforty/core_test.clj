(ns tenforty.core-test
  (:require [clojure.test :refer :all]
            [tenforty.core :refer :all]))

(deftest data-deps
  (testing "Get data dependencies from a quoted form"
    (is (= #{:AGI}) (data-dependencies '(cell-value :AGI)))
    (is (= #{} (data-dependencies '(+ 1 1))))
    (is (= #{} (data-dependencies '(identity 0))))
    (is (= #{:AGI} (data-dependencies '(+ (cell-value :AGI) 1))))
    (is (= #{:pretotal_tax :total_credits} (data-dependencies '(- (cell-value :pretotal_tax) (cell-value :total_credits)))))
    (is (= #{:AGI} (data-dependencies '(let [temp (cell-value :AGI)] temp))))))

(deftest make-formula-line-no-deps
  (testing "Macro to create a line with a degenerate expression"
    (let [myline
          (make-formula-line ::myline 0)]
      (is (= ::myline (:kw myline)))
      (is (fn? (:fn myline)))
      (is (= #{} (:deps myline)))
      (is (== 0 ((:fn myline) (fn [kw] 1234)))))))

(deftest make-formula-line-test
  (testing "Macro to create a line with an expression"
    (let [tax_minus_credits
          (make-formula-line :tenforty.forms.ty2015.f1040/tax_minus_credits (max (- (cell-value :tenforty.forms.ty2015.f1040/pretotal_tax) (cell-value :tenforty.forms.ty2015.f1040/total_credits) (cell-value :tenforty.forms.ty2015.s8812/ctc)) 0))]
      (is (= :tenforty.forms.ty2015.f1040/tax_minus_credits) (:kw tax_minus_credits))
      (is (fn? (:fn tax_minus_credits)))
      (is (= #{:tenforty.forms.ty2015.f1040/pretotal_tax :tenforty.forms.ty2015.f1040/total_credits :tenforty.forms.ty2015.s8812/ctc} (:deps tax_minus_credits)))
      (is (== 0 ((:fn tax_minus_credits) (fn [kw] 0))))
      (is (== 0 ((:fn tax_minus_credits) (fn [kw] 1))))
      (is (== 1 ((:fn tax_minus_credits) (fn [kw] -1)))))))

(deftest defform-test
  (defform)
  (is (= {} form))
  (defform
    nil
    [(make-formula-line ::refund (max (- (cell-value ::total_payments) (cell-value ::total_tax)) 0))])
  (is (= ::refund (:kw (::refund (get form nil)))))
  (is (= #{::total_payments ::total_tax} (:deps (::refund (get form nil))))))

(deftest duplicate-line-test
  (is (thrown? IllegalArgumentException
               (defform
                 nil
                 [(make-input-line ::a)
                  (make-input-line ::a)])))
  (is (thrown? IllegalArgumentException
               (defform
                 :f1
                 [(make-input-line ::a)]
                 :f2
                 [(make-input-line ::a)]))))

(deftest duplicate-group-test
  (is (thrown? IllegalArgumentException
               (defform
                 :f1
                 []
                 :f1
                 []))))

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

(deftest calculate-test
  (let [form {:a (make-formula-line :a (if (cell-value :b)
                                (cell-value :c)
                                (cell-value :d)))
              :b (make-boolean-input-line :b)
              :c (make-input-line :c)
              :d (make-input-line :d)
              :e (make-formula-line :e (cell-value :z))
              :z (make-input-line :z)
              :true (make-boolean-input-line :true)
              :false (make-boolean-input-line :false)}
        situation (->MapTaxSituation {:b true
                                      :c 5
                                      :d 10
                                      :true true
                                      :false false})]
    (is (= 5 (calculate form :a situation)))
    (is (= true (calculate form :b situation)))
    (is (= 5 (calculate form :c situation)))
    (is (= 10 (calculate form :d situation)))
    (is (thrown? Throwable (calculate form :e situation)))
    (is (= true (calculate form :true situation)))
    (is (= false (calculate form :false situation)))
    (let [ctx (make-context form situation)]
      (calculate ctx :a)
      (is (= 5 (:a @(:cache ctx))))
      (is (= 5 (calculate ctx :a))))))
