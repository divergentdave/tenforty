(ns tenforty.forms.ty2016.rrb1099
  (:require [tenforty.core :refer [defform
                                   make-number-input-line
                                   make-formula-line]
             :include-macros true]))

(defform
  [nil #{:rrb1099}]
  []
  [:rrb1099 #{}]
  [(make-number-input-line ::employee_contributions)
   (make-number-input-line ::contributory_amount_paid)
   (make-number-input-line ::vested_dual_benefit)
   (make-number-input-line ::supplemental_annuity)
   (make-formula-line ::total_gross_paid
                      (+ (cell-value ::contributory_amount_paid)
                         (cell-value ::vested_dual_benefit)
                         (cell-value ::supplemental_annuity)))
   (make-number-input-line ::repayments)
   (make-number-input-line ::federal_income_tax_withheld)
   (make-number-input-line ::rate_of_tax)
   (make-number-input-line ::medicare_premium_total)])
