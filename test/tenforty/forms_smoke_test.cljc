(ns tenforty.forms-smoke-test
  (:require [clojure.test :refer [deftest
                                  testing
                                  is]
             :include-macros true]
            [tenforty.core :refer [GroupValues
                                   lookup-value
                                   lookup-group
                                   make-context
                                   calculate
                                   #?@(:cljs
                                       [BooleanInputLine
                                        CodeInputLine
                                        NumberInputLine])]]
            [tenforty.forms.ty2015]
            [tenforty.forms.ty2016])
  #?(:clj (:import [tenforty.core
                    BooleanInputLine
                    CodeInputLine
                    NumberInputLine])))

(defrecord SmokeTestTaxSituation [forms]
  GroupValues
  (lookup-value [self kw]
    (let [line (get (:lines (:forms self)) kw)]
      (cond
        (instance? BooleanInputLine line)
        false
        (instance? CodeInputLine line)
        (first (vals (:options line)))
        (instance? NumberInputLine line)
        0)))
  (lookup-group [self kw]
    [self]))

(deftest form-line-smoke-test-2015
  (let [situation (->SmokeTestTaxSituation tenforty.forms.ty2015/forms)
        context (make-context tenforty.forms.ty2015/forms situation)
        lines (vals (:lines tenforty.forms.ty2015/forms))]
    (dorun (map (fn [line]
                  (let [kw (:kw line)]
                    (testing (str "Evaluate " kw " with zeros")
                      (is (not (nil? (calculate context kw)))))))
                lines))))

(deftest form-line-smoke-test-2016
  (let [situation (->SmokeTestTaxSituation tenforty.forms.ty2016/forms)
        context (make-context tenforty.forms.ty2016/forms situation)
        lines (vals (:lines tenforty.forms.ty2016/forms))]
    (dorun (map (fn [line]
                  (let [kw (:kw line)]
                    (testing (str "Evaluate " kw " with zeros")
                      (is (not (nil? (calculate context kw)))))))
                lines))))
