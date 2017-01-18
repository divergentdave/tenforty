(ns tenforty.forms.ty2016.f2441
  (:require [tenforty.core :refer [defform
                                   make-number-input-line
                                   make-boolean-input-line
                                   make-formula-line]
             :include-macros true]
            [tenforty.forms.ty2016.f1040 :refer [SINGLE
                                                 MARRIED_FILING_JOINTLY
                                                 MARRIED_FILING_SEPARATELY
                                                 HEAD_OF_HOUSEHOLD
                                                 QUALIFYING_WIDOW_WIDOWER]]))

(defform
  [nil #{}]
  [(make-number-input-line ::qualifying_persons_number)
   (make-number-input-line ::self_employed_dependent_care_assistance_program)
   (make-number-input-line ::self_employed_dependent_care_assistance_program_from_sole_proprietorship_or_partnership)
   (make-formula-line ::dependent_care_benefits
                      (+ (apply + (cell-value :tenforty.forms.ty2016.w2/dependent_care_benefits))
                         (cell-value ::self_employed_dependent_care_assistance_program)))
   (make-number-input-line ::carryover_previous)
   (make-number-input-line ::forfeited_or_carryover_future)
   (make-formula-line ::combined_benefits (- (+ (cell-value ::dependent_care_benefits)
                                                (cell-value ::carryover_previous))
                                             (cell-value ::forfeited_or_carryover_future)))
   (make-number-input-line ::qualifying_expenses)
   (make-formula-line ::lesser_combined_benefits_qualifying_expenses
                      (min (cell-value ::combined_benefits)
                           (cell-value ::qualifying_expenses)))
   (make-number-input-line ::earned_income)
   (make-number-input-line ::spouse_earned_income)
   (make-boolean-input-line ::considered_unmarried)
   (make-formula-line ::spouse_earned_income_effective
                      (condp = (cell-value :tenforty.forms.ty2016.f1040/filing_status)
                        MARRIED_FILING_SEPARATELY
                        (if (cell-value ::considered_unmarried)
                          (cell-value ::earned_income)
                          (cell-value ::spouse_earned_income))
                        MARRIED_FILING_JOINTLY
                        (cell-value ::spouse_earned_income)
                        HEAD_OF_HOUSEHOLD
                        (cell-value ::earned_income)
                        SINGLE
                        (cell-value ::earned_income)
                        QUALIFYING_WIDOW_WIDOWER
                        (cell-value ::earned_income)))
   (make-formula-line ::line_20
                      (min (cell-value ::lesser_combined_benefits_qualifying_expenses)
                           (cell-value ::earned_income)
                           (cell-value ::spouse_earned_income_effective)))
   (make-formula-line ::line_21
                      (if (and (= MARRIED_FILING_SEPARATELY (cell-value :tenforty.forms.ty2016.f1040/filing_status))
                               (not (cell-value ::considered_unmarried)))
                        2500
                        5000))
   (make-formula-line ::deductible_benefits
                      (min (cell-value ::line_20)
                           (cell-value ::line_21)
                           (cell-value ::self_employed_dependent_care_assistance_program_from_sole_proprietorship_or_partnership)))
   (make-formula-line ::excluded_benefits
                      (max (- (min (cell-value ::line_20)
                                   (cell-value ::line_21))
                              (cell-value ::deductible_benefits))
                           0))
   (make-formula-line ::taxable_benefits
                      (max (- (- (cell-value ::combined_benefits)
                                 (cell-value ::self_employed_dependent_care_assistance_program_from_sole_proprietorship_or_partnership))
                              (cell-value ::excluded_benefits))
                           0))

   (make-formula-line ::line_29
                      (max (- (cond
                                (>= (cell-value ::qualifying_persons_number) 2)
                                6000
                                (= (cell-value ::qualifying_persons_number) 1)
                                3000
                                true
                                0)
                              (+ (cell-value ::deductible_benefits)
                                 (cell-value ::excluded_benefits)))
                           0))
   (make-formula-line ::line_31
                      (min (cell-value ::line_29)
                           (cell-value ::qualifying_expenses)))
   (make-formula-line ::line_6
                      (min (cell-value ::line_31)
                           (cell-value ::earned_income)
                           (if (= (cell-value :tenforty.forms.ty2016.f1040/filing_status) MARRIED_FILING_JOINTLY)
                             (cell-value ::spouse_earned_income)
                             (cell-value ::earned_income))))
   (make-formula-line ::credit_limit
                      (max 0 (- (cell-value ::tenforty.forms.ty2016.f1040/pretotal_tax)
                                (cell-value ::tenforty.forms.ty2016.f1040/foreign_tax_credit))))
   (make-formula-line ::child_dependent_care_expenses_credit
                      (min (* (cell-value ::line_6)
                              (condp > (cell-value :tenforty.forms.ty2016.f1040/agi)
                                15000 0.35
                                17000 0.34
                                19000 0.33
                                21000 0.32
                                23000 0.31
                                25000 0.30
                                27000 0.29
                                29000 0.28
                                31000 0.27
                                33000 0.26
                                35000 0.25
                                37000 0.24
                                39000 0.23
                                41000 0.22
                                43000 0.21
                                0.20))
                           (cell-value ::credit_limit)))])
; TODO: ignores "Credit for Prior Year's Expenses" procedures, very complicated
