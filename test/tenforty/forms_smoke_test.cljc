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

(defn- build-contexts [group-kw parent-context base-situation]
  (let [form-subgraph (:forms base-situation)
        groups (:groups form-subgraph)
        base-context (make-context form-subgraph base-situation group-kw parent-context)]
    (apply merge (sorted-map group-kw base-context)
           (map #(build-contexts % base-context (first (lookup-group base-situation %)))
                (get groups group-kw)))))

(deftest form-line-smoke-test-2016
  (let [forms tenforty.forms.ty2016/forms
        situation (->SmokeTestTaxSituation forms)
        lines (vals (:lines forms))
        contexts (build-contexts nil nil situation)]
    (dorun (map (fn [line]
                  (let [kw (:kw line)
                        group (:group line)
                        context (get contexts group)]
                    (testing (str "Evaluate " kw " with zeros")
                      (is (not (nil? (calculate context kw)))))))
                lines))))
