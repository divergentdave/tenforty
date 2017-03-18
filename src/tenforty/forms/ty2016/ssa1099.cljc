(ns tenforty.forms.ty2016.ssa1099
  (:require [tenforty.core :refer [defform
                                   make-number-input-line
                                   make-formula-line]
             :include-macros true]))

(defform
  [nil #{:ssa1099}]
  []
  [:ssa1099 #{}]
  [(make-number-input-line ::benefits_paid)
   (make-number-input-line ::benefits_repaid)
   (make-formula-line ::net_benefits (- (cell-value ::benefits_paid)
                                        (cell-value ::benefits_repaid)))
   (make-number-input-line ::voluntary_federal_income_tax_withheld)])
