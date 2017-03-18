(ns tenforty.forms.ty2016.f8919
  (:require [tenforty.core :refer [defform
                                   make-number-input-line
                                   make-formula-line]
             :include-macros true]))

(defform
  [nil #{:f8919}]
  [(make-formula-line ::wages_without_withholding_total
                      (apply + (cell-value ::wages_without_withholding)))
   (make-formula-line ::wages_without_withholding_social_security
                      (min (cell-value ::wages_without_withholding_total)
                           (max (- 118500
                                   (+ (apply + (cell-value :tenforty.forms.ty2016.w2/social_security_wages))
                                      (apply + (cell-value :tenforty.forms.ty2016.w2/social_security_tips))
                                      (min 118500 (apply + (cell-value :tenforty.forms.ty2016.rrb1099/total_gross_paid)))
                                      (apply + (cell-value :tenforty.forms.ty2016.f4137/social_security_unreported_tips))))
                                0)))
   (make-formula-line ::unreported_social_security_medicare_tax
                      (+ (* 0.062 (cell-value ::wages_without_withholding_social_security))
                         (* 0.0145 (cell-value ::wages_without_withholding_total))))]
  [:f8919 #{:f8919_employer}]
  [(make-number-input-line ::wages_without_withholding)])
