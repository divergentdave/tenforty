(ns tenforty.forms-smoke-test
  (:require [clojure.test :refer [deftest
                                  testing
                                  is]
             :include-macros true]
            [tenforty.core :refer [GroupValues
                                   lookup-value
                                   lookup-group
                                   ->ZeroTaxSituation
                                   make-context
                                   calculate]]
            [tenforty.forms.ty2015]
            [tenforty.forms.ty2016]))

(defrecord SmokeTestTaxSituation []
  GroupValues
  (lookup-value [self kw]
    (condp = kw
      :tenforty.forms.ty2015.f1040/filing_status 1
      :tenforty.forms.ty2015.f1040/last_year_filing_status 1
      :tenforty.forms.ty2015.f1040/senior false
      :tenforty.forms.ty2015.f1040/spouse_senior false
      :tenforty.forms.ty2015.f1040/blind false
      :tenforty.forms.ty2015.f1040/spouse_blind false
      :tenforty.forms.ty2016.f1040/filing_status 1
      :tenforty.forms.ty2016.f1040/last_year_filing_status 1
      :tenforty.forms.ty2016.f1040/senior false
      :tenforty.forms.ty2016.f1040/spouse_senior false
      :tenforty.forms.ty2016.f1040/blind false
      :tenforty.forms.ty2016.f1040/spouse_blind false
      0))
  (lookup-group [self kw]
    [(->ZeroTaxSituation)]))

(deftest form-line-smoke-test-2015
  (let [situation (->SmokeTestTaxSituation)
        context (make-context tenforty.forms.ty2015/forms situation)
        lines (vals (:lines tenforty.forms.ty2015/forms))]
    (dorun (map (fn [line]
                  (let [kw (:kw line)]
                    (testing (str "Evaluate " kw " with zeros")
                      (is (not (nil? (calculate context kw)))))))
                lines))))

(deftest form-line-smoke-test-2016
  (let [situation (->SmokeTestTaxSituation)
        context (make-context tenforty.forms.ty2016/forms situation)
        lines (vals (:lines tenforty.forms.ty2016/forms))]
    (dorun (map (fn [line]
                  (let [kw (:kw line)]
                    (testing (str "Evaluate " kw " with zeros")
                      (is (not (nil? (calculate context kw)))))))
                lines))))
