(ns tenforty.forms.ty2016.f1099r
  (:require [tenforty.core :refer [defform
                                   make-number-input-line]
             :include-macros true]))

(defform
  [nil #{:1099r}]
  []
  [:1099r #{}]
  [(make-number-input-line ::gross_distribution)
   (make-number-input-line ::taxable_amount)
   (make-number-input-line ::capital_gain)
   (make-number-input-line ::federal_income_tax_withheld)
   (make-number-input-line ::employee_contributions)
   (make-number-input-line ::net_unrealized_appreciation)
   (make-number-input-line ::total_employee_contributions)
   (make-number-input-line ::allocable_to_irr_5_years)
   (make-number-input-line ::state_tax_withheld)
   (make-number-input-line ::state_distribution)
   (make-number-input-line ::local_tax_withheld)
   (make-number-input-line ::local_distribution)])
