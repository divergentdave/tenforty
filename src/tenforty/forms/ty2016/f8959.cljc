(ns tenforty.forms.ty2016.f8959
  (:require [tenforty.core :refer [defform
                                   make-number-input-line
                                   make-formula-line]
             :include-macros true])
  (:require [tenforty.forms.ty2016.f1040 :refer [SINGLE
                                                 MARRIED_FILING_JOINTLY
                                                 MARRIED_FILING_SEPARATELY
                                                 HEAD_OF_HOUSEHOLD
                                                 QUALIFYING_WIDOW_WIDOWER]]))

(defform
  [nil #{}]
  [(make-formula-line ::medicare_wages_tips
                      (+ (apply + (cell-value :tenforty.forms.ty2016.w2/medicare_wages_tips))
                         (apply + (cell-value :tenforty.forms.ty2016.f4137/medicare_unreported_tips))
                         (apply + (cell-value :tenforty.forms.ty2016.f8919/wages_without_withholding_total))))
   (make-formula-line ::additional_medicare_tax_threshold
                      (condp = (cell-value :tenforty.forms.ty2016.f1040/filing_status)
                        MARRIED_FILING_JOINTLY 250000
                        MARRIED_FILING_SEPARATELY 125000
                        SINGLE 200000
                        HEAD_OF_HOUSEHOLD 200000
                        QUALIFYING_WIDOW_WIDOWER 200000))
   (make-formula-line ::additional_medicare_tax_on_medicare_wages
                      (* 0.009
                         (max (- (cell-value ::medicare_wages_tips)
                                 (cell-value ::additional_medicare_tax_threshold))
                              0)))
   (make-number-input-line ::medicare_self_employment_income) ; TODO
   (make-formula-line ::additional_medicare_tax_on_self_employment_income
                      (* 0.009
                         (max (- (cell-value ::medicare_self_employment_income)
                                 (max (- (cell-value ::additional_medicare_tax_threshold)
                                         (cell-value ::medicare_wages_tips)))))))
   (make-number-input-line ::rrta_compensation_and_tips) ; TODO
   (make-number-input-line ::rrta_additional_medicare_tax_withholding) ; TODO
   (make-formula-line ::additional_medicare_tax_on_rrta_compensation
                      (* 0.009
                         (max (- (cell-value ::rrta_compensation_and_tips)
                                 (cell-value ::additional_medicare_tax_threshold)))))
   (make-formula-line ::additional_medicare_tax
                      (+ (cell-value ::additional_medicare_tax_on_medicare_wages)
                         (cell-value ::additional_medicare_tax_on_self_employment_income)
                         (cell-value ::additional_medicare_tax_on_rrta_compensation)))
   (make-formula-line ::total_additional_medicare_tax_withholding
     (+ (max (- (apply + (cell-value :tenforty.forms.ty2016.w2/medicare_tax_withheld))
                (* 0.0145 (apply + (cell-value :tenforty.forms.ty2016.w2/medicare_wages_tips))))
                0)
        (cell-value ::rrta_additional_medicare_tax_withholding)))])
