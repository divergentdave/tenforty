(ns tenforty.core-test
  (:require [clojure.test :refer [deftest
                                  testing
                                  is]
             :include-macros true]
            [tenforty.core :refer [data-dependencies
                                   make-form-subgraph
                                   ->FormSubgraph
                                   make-input-line
                                   make-code-input-line
                                   make-boolean-input-line
                                   make-formula-line
                                   get-group
                                   lookup-value
                                   lookup-group
                                   ->ZeroTaxSituation
                                   ->MapTaxSituation
                                   ->EdnTaxSituation
                                   calculate
                                   make-context]
             :include-macros true]))

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

(deftest make-form-subgraph-test
  (let [form (make-form-subgraph)]
    (is (= (->FormSubgraph {} {}) form)))
  (let [form (make-form-subgraph
              [nil #{:mygroup}]
              []
              [:mygroup #{}]
              [(make-formula-line ::refund (max (- (cell-value ::total_payments) (cell-value ::total_tax)) 0))])]
    (is (= ::refund (:kw (::refund (:lines form)))))
    (is (= #{::total_payments ::total_tax} (:deps (::refund (:lines form)))))
    (is (= :mygroup (get-group (::refund (:lines form)))))
    (is (= {nil #{:mygroup} :mygroup #{}} (:groups form)))))

(deftest duplicate-line-test
  (is (thrown? #? (:clj IllegalArgumentException :cljs :default)
               (make-form-subgraph
                [nil #{}]
                [(make-input-line ::a)
                 (make-input-line ::a)])))
  (is (thrown? #? (:clj IllegalArgumentException :cljs :default)
               (make-form-subgraph
                [nil #{:f1 :f2}]
                []
                [:f1 #{}]
                [(make-input-line ::a)]
                [:f2 #{}]
                [(make-input-line ::a)]))))

(deftest zero-tax-situation-test
  (is (= 0 (lookup-value (->ZeroTaxSituation) ::a))))

(deftest map-tax-situation-test
  (let [situation (->MapTaxSituation {::a 10} {})]
    (is (= 10 (lookup-value situation ::a)))))

(deftest edn-tax-situation-test
  (let [form (make-form-subgraph
               [nil #{:g}]
               [(make-input-line :a)
                (make-formula-line :b (apply + (cell-value :c)))]
               [:g #{}]
               [(make-input-line :c)])
        situation (->EdnTaxSituation {:values {:a 17}
                                      :groups {:g [{:values {:c 1}
                                                    :groups {}}
                                                   {:values {:c 2}
                                                    :groups {}}]}})]
    (is (= 17 (calculate form :a situation)))
    (is (= 3 (calculate form :b situation)))))

(deftest calculate-test
  (let [form (->FormSubgraph
              {:a (make-formula-line :a (if (cell-value :b)
                                          (cell-value :c)
                                          (cell-value :d)))
               :b (make-boolean-input-line :b)
               :c (make-input-line :c)
               :d (make-input-line :d)
               :e (make-formula-line :e (cell-value :z))
               :z (make-input-line :z)
               :true (make-boolean-input-line :true)
               :false (make-boolean-input-line :false)}
              {nil #{}})
        situation (->MapTaxSituation {:b true
                                      :c 5
                                      :d 10
                                      :true true
                                      :false false} {})]
    (is (= 5 (calculate form :a situation)))
    (is (= true (calculate form :b situation)))
    (is (= 5 (calculate form :c situation)))
    (is (= 10 (calculate form :d situation)))
    (is (thrown? #? (:clj Throwable :cljs :default)
                 (calculate form :e situation)))
    (is (= true (calculate form :true situation)))
    (is (= false (calculate form :false situation)))
    (let [ctx (make-context form situation)]
      (calculate ctx :a)
      (is (= 5 (:a @(:value-cache ctx))))
      (is (= 5 (calculate ctx :a))))))

(deftest calculate-groups-test
  (let [form (make-form-subgraph
              [nil #{:grp}]
              [(make-formula-line :clamp 100)
               (make-formula-line :sum (apply + (cell-value :entry)))
               (make-formula-line :sum_clamped (apply + (cell-value :clamped)))]
              [:grp #{}]
              [(make-input-line :entry)
               (make-formula-line :clamped (min (cell-value :entry) (cell-value :clamp)))])
        situation (->MapTaxSituation
                   {} {:grp [(->MapTaxSituation
                              {:entry 5} {})
                             (->MapTaxSituation
                              {:entry 13} {})
                             (->MapTaxSituation
                              {:entry 101} {})]})]
    (is (= 119 (calculate form :sum situation)))
    (is (= 118 (calculate form :sum_clamped situation))))
  (let [form (make-form-subgraph
              [nil #{:child}]
              [(make-formula-line :a (cell-value :b))]
              [:child #{:grandchild}]
              []
              [:granchild #{}]
              [(make-input-line :b)])]
    (is (thrown? #? (:clj Throwable :cljs :default)
                 (calculate form :a (->ZeroTaxSituation)))))
  (let [form (make-form-subgraph
              [nil #{:child}]
              [(make-formula-line :x (apply + (cell-value :y)))]
              [:child #{:grandchild}]
              [(make-formula-line :y (apply + (cell-value :z)))]
              [:grandchild #{}]
              [(make-input-line :z)])
        situation (->MapTaxSituation
                   {} {:child [(->MapTaxSituation
                                {} {:grandchild [(->MapTaxSituation
                                                  {:z 1} {})
                                                 (->MapTaxSituation
                                                  {:z 2} {})]})
                               (->MapTaxSituation
                                {} {:grandchild [(->MapTaxSituation
                                                  {:z 3} {})
                                                 (->MapTaxSituation
                                                  {:z 4} {})]})]})]
    (is (= 10 (calculate form :x situation)))))
