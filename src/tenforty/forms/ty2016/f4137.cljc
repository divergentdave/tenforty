(ns tenforty.forms.ty2016.f4137
  (:require [tenforty.core :refer [defform
                                   make-number-input-line
                                   make-formula-line]
             :include-macros true]))

(defform
  [nil #{:f4137}]
  [(make-formula-line ::social_security_unreported_tips_total
                      (apply + (cell-value ::social_security_unreported_tips)))]
  [:f4137 #{:f4137_employer}]
  [(make-formula-line ::actual_tips_total (apply + (cell-value ::actual_tips)))
   (make-formula-line ::reported_tips_total (apply + (cell-value ::reported_tips)))
   (make-formula-line ::unreported_tips
                      (- (cell-value ::actual_tips_total)
                         (cell-value ::reported_tips_total)))
   (make-number-input-line ::unreported_tips_below_threshold)
   (make-formula-line ::medicare_unreported_tips
                      (- (cell-value ::unreported_tips)
                         (cell-value ::unreported_tips_below_threshold)))
   (make-formula-line ::social_security_unreported_tips
                      (min (cell-value ::medicare_unreported_tips)
                           (max (- 118500
                                   (+ (cell-value :tenforty.forms.ty2016.w2/social_security_wages_total)
                                      (cell-value :tenforty.forms.ty2016.w2/social_security_tips_total)
                                      (min 118500 (cell-value :tenforty.forms.ty2016.rrb1099/total_gross_paid_total))))
                                0)))
   (make-formula-line ::unreported_social_security_medicare_tax
                      (+ (* 0.062 (cell-value ::social_security_unreported_tips))
                         (* 0.0145 (cell-value ::medicare_unreported_tips))))]
  [:f4137_employer #{}]
  [(make-number-input-line ::actual_tips)
   (make-number-input-line ::reported_tips)])
