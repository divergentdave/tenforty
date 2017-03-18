(ns tenforty.forms.ty2016.w2g
  (:require [tenforty.core :refer [defform
                                   make-number-input-line]
             :include-macros true]))

(defform
  [nil #{:w2g}]
  []
  [:w2g #{}]
  [(make-number-input-line ::reportable_winnings)
   (make-number-input-line ::federal_income_tax_withheld)
   (make-number-input-line ::winnings_from_identical_wagers)
   (make-number-input-line ::state_winnings)
   (make-number-input-line ::state_income_tax_withheld)
   (make-number-input-line ::local_winnings)
   (make-number-input-line ::local_income_tax_withheld)])
