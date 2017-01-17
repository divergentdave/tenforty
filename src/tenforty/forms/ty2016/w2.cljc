(ns tenforty.forms.ty2016.w2
  (:require [tenforty.core :refer [defform
                                   make-number-input-line]
             :include-macros true]))

(defform
  [nil #{:w2}]
  []
  [:w2 #{}]
  [(make-number-input-line ::wages_tips_other)
   (make-number-input-line ::federal_income_tax_withheld)
   (make-number-input-line ::social_security_wages)
   (make-number-input-line ::social_security_tax_withheld)
   (make-number-input-line ::medicare_wages_tips)
   (make-number-input-line ::medicare_tax_withheld)
   (make-number-input-line ::social_security_tips)
   (make-number-input-line ::allocated_tips)
   (make-number-input-line ::dependent_care_benefits)
   (make-number-input-line ::nonqualified_plans)
   (make-number-input-line ::code_t_adoption_benefits)])
