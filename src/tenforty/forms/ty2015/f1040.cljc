(ns tenforty.forms.ty2015.f1040
  (:require [tenforty.core :refer [defform
                                   make-number-input-line
                                   make-code-input-line
                                   make-boolean-input-line
                                   make-formula-line]
             :include-macros true]
            [tenforty.math :refer [ceil]]
            [tenforty.forms.ty2015.s8812]))

; Filing status codes
(def ^:const SINGLE 1)
(def ^:const MARRIED_FILING_JOINTLY 2)
(def ^:const MARRIED_FILING_SEPARATELY 3)
(def ^:const HEAD_OF_HOUSEHOLD 4)
(def ^:const QUALIFYING_WIDOW_WIDOWER 5)
(def ^:const FILING_STATUSES {"Single" SINGLE
                              "Married filing jointly" MARRIED_FILING_JOINTLY
                              "Married filing separately" MARRIED_FILING_SEPARATELY
                              "Head of household" HEAD_OF_HOUSEHOLD
                              "Qualifying widow(er)" QUALIFYING_WIDOW_WIDOWER})

(defform
  [nil #{}]
  [(make-code-input-line ::filing_status FILING_STATUSES) ; IRS1040/IndividualReturnFilingStatusCd/text()
   (make-boolean-input-line ::exemption_self)
   (make-boolean-input-line ::exemption_spouse)
   (make-number-input-line ::dependents)
   (make-formula-line ::exemptions_number (+ (if (cell-value ::exemption_self) 1 0)
                                             (if (cell-value ::exemption_spouse) 1 0)
                                             (cell-value ::dependents)))

   (make-number-input-line ::wages) ; TODO
   (make-number-input-line ::taxable_interest) ; TODO
   (make-number-input-line ::tax_exempt_interest) ; TODO
   (make-number-input-line ::ordinary_dividends) ; TODO
   (make-number-input-line ::qualified_dividends) ; TODO

   (make-number-input-line ::last_year_state_local_refund)
   (make-number-input-line ::last_year_itemized_deductions)
   (make-code-input-line ::last_year_filing_status FILING_STATUSES)
   (make-boolean-input-line ::last_year_senior)
   (make-boolean-input-line ::last_year_spouse_senior)
   (make-boolean-input-line ::last_year_blind)
   (make-boolean-input-line ::last_year_spouse_blind)
   (make-formula-line ::taxable_tax_refunds ; TODO: The instructions are full of exceptions that aren't captured here yet, see publication 525 for complete details
                      (min (cell-value ::last_year_state_local_refund)
                           (max (- (cell-value ::last_year_itemized_deductions)
                                   (condp = (cell-value ::last_year_filing_status)
                                     SINGLE 6200
                                     MARRIED_FILING_SEPARATELY 6200
                                     MARRIED_FILING_JOINTLY 12400
                                     QUALIFYING_WIDOW_WIDOWER 12400
                                     HEAD_OF_HOUSEHOLD 9100)
                                   (* (+ (if (cell-value ::last_year_senior) 1 0)
                                         (if (cell-value ::last_year_spouse_senior) 1 0)
                                         (if (cell-value ::last_year_blind) 1 0)
                                         (if (cell-value ::last_year_spouse_blind) 1 0))
                                      (condp = (cell-value ::last_year_filing_status)
                                        MARRIED_FILING_SEPARATELY 1200
                                        MARRIED_FILING_JOINTLY 1200
                                        QUALIFYING_WIDOW_WIDOWER 1200
                                        SINGLE 1550
                                        HEAD_OF_HOUSEHOLD 1550))) 0))) ; IRS1040/StateLocalIncomeTaxRefundAmt/text()

   (make-number-input-line ::alimony_received) ; TODO
   (make-number-input-line ::business_income_loss) ; TODO
   (make-number-input-line ::capital_gain_loss) ; TODO
   (make-number-input-line ::other_gain_loss) ; TODO
   (make-number-input-line ::ira_distributions) ; TODO
   (make-number-input-line ::ira_distributions_taxable) ; TODO
   (make-number-input-line ::pensions_annuities) ; TODO
   (make-number-input-line ::pensions_annuities_taxable) ; TODO
   (make-number-input-line ::schedule_e_income) ; TODO
   (make-number-input-line ::farm_income_loss) ; TODO
   (make-number-input-line ::unemployment_compensation) ; TODO
   (make-number-input-line ::social_security_benefits) ; TODO
   (make-number-input-line ::social_security_benefits_taxable) ; TODO
   (make-number-input-line ::other_income) ; TODO

   (make-formula-line ::magi_total_income (+ (cell-value ::wages)
                                             (cell-value ::taxable_interest)
                                             (cell-value ::ordinary_dividends)
                                             (cell-value ::taxable_tax_refunds)
                                             (cell-value ::alimony_received)
                                             (cell-value ::business_income_loss)
                                             (cell-value ::capital_gain_loss)
                                             (cell-value ::ira_distributions_taxable)
                                             (cell-value ::pensions_annuities_taxable)
                                             (cell-value ::farm_income_loss)
                                             (cell-value ::unemployment_compensation)
                                             (cell-value ::social_security_benefits_taxable)
                                             (cell-value ::other_income)))
   (make-formula-line ::total_income (+ (cell-value ::magi_total_income)
                                        (cell-value ::schedule_e_income)))

   (make-number-input-line ::educator_expenses) ; TODO
   (make-number-input-line ::expenses_2106) ; TODO
   (make-number-input-line ::hsa_deduction) ; TODO
   (make-number-input-line ::moving_expenses) ; TODO
   (make-number-input-line ::self_employment_tax_deductible) ; TODO
   (make-number-input-line ::self_employed_pension_deduction) ; TODO
   (make-number-input-line ::self_employed_health_insurance_deduction) ; TODO
   (make-number-input-line ::early_withdrawl_penalty) ; TODO
   (make-number-input-line ::alimony_paid) ; TODO
   (make-number-input-line ::ira_deduction) ; TODO

   (make-number-input-line ::student_loan_interest)
   (make-formula-line ::student_loan_interest_deduction
                      (let [loans_maxed (min (cell-value ::student_loan_interest) 2500)
                            income_limit (if (= (cell-value ::filing_status)
                                                MARRIED_FILING_JOINTLY)
                                           130000
                                           65000)
                            diff (max (- (cell-value ::total_income) income_limit) 0)
                            diff_divided (/ diff (if (= (cell-value ::filing_status)
                                                        MARRIED_FILING_JOINTLY)
                                                   30000
                                                   15000))
                            phased_out_loans (* loans_maxed diff_divided)]
                        (max
                         (- loans_maxed phased_out_loans)
                         0)))

   (make-number-input-line ::tuition_fees_deduction) ; TODO
   (make-number-input-line ::domestic_production_activities_deduction) ; TODO
   (make-formula-line ::agi_deductions (+ (cell-value ::educator_expenses)
                                          (cell-value ::expenses_2106)
                                          (cell-value ::hsa_deduction)
                                          (cell-value ::moving_expenses)
                                          (cell-value ::self_employment_tax_deductible)
                                          (cell-value ::self_employed_pension_deduction)
                                          (cell-value ::self_employed_health_insurance_deduction)
                                          (cell-value ::early_withdrawl_penalty)
                                          (cell-value ::alimony_paid)
                                          (cell-value ::ira_deduction)
                                          (cell-value ::student_loan_interest_deduction)
                                          (cell-value ::tuition_fees_deduction)
                                          (cell-value ::domestic_production_activities_deduction)))
   (make-formula-line ::agi (- (cell-value ::total_income) (cell-value ::agi_deductions)))

   (make-boolean-input-line ::senior)
   (make-boolean-input-line ::spouse_senior)
   (make-boolean-input-line ::blind)
   (make-boolean-input-line ::spouse_blind)
   (make-boolean-input-line ::spouse_itemizes_separately)
   (make-boolean-input-line ::dual_status_alien)
   (make-formula-line ::senior_blind_total (+ (if (cell-value ::senior) 1 0)
                                              (if (cell-value ::spouse_senior) 1 0)
                                              (if (cell-value ::blind) 1 0)
                                              (if (cell-value ::spouse_blind) 1 0)))
   (make-formula-line ::earned_income (+ (cell-value ::wages)
                                         (cell-value ::business_income_loss)
                                         (cell-value ::farm_income_loss)
                                         (- (cell-value ::self_employment_tax_deductible))))
   (make-formula-line ::standard_deduction (cond
                                   ; Exception 1 - dependent
                                             (if (= (cell-value ::filing_status) MARRIED_FILING_JOINTLY)
                                               (not (and (cell-value ::exemption_self) (cell-value ::exemption_spouse)))
                                               (not (cell-value ::exemption_self)))
                                             (+ ; Standard Deduction Worksheet for Dependents
                                              (min (+ 350 (max 700 (cell-value ::earned_income)))
                                                   (condp = (cell-value ::filing_status)
                                                     SINGLE 6300
                                                     MARRIED_FILING_SEPARATELY 6300
                                                     MARRIED_FILING_JOINTLY 12600
                                                     QUALIFYING_WIDOW_WIDOWER 12600 ; not on worksheet, but inferred
                                                     HEAD_OF_HOUSEHOLD 9250))
                                              (* (cell-value ::senior_blind_total)
                                                 (condp = (cell-value ::filing_status)
                                                   MARRIED_FILING_JOINTLY 1250
                                                   QUALIFYING_WIDOW_WIDOWER 1250
                                                   MARRIED_FILING_SEPARATELY 1250
                                                   SINGLE 1550
                                                   HEAD_OF_HOUSEHOLD 1550)))
                                   ; Exception 2 - box on line 39a checked
                                             (> (cell-value ::senior_blind_total) 0)
                                             (condp = (cell-value ::filing_status)
                                               SINGLE (case (cell-value ::senior_blind_total)
                                                        1 7850
                                                        2 9400)
                                               MARRIED_FILING_JOINTLY (case (cell-value ::senior_blind_total)
                                                                        1 13850
                                                                        2 15100
                                                                        3 16350
                                                                        4 17600)
                                               QUALIFYING_WIDOW_WIDOWER (case (cell-value ::senior_blind_total)
                                                                          1 13850
                                                                          2 15100
                                                                          3 16350
                                                                          4 17600)
                                               MARRIED_FILING_SEPARATELY (case (cell-value ::senior_blind_total)
                                                                           1 7550
                                                                           2 8800
                                                                           3 10050
                                                                           4 11300)
                                               HEAD_OF_HOUSEHOLD (case (cell-value ::senior_blind_total)
                                                                   1 10800
                                                                   2 12350))
                                   ; Exception 3 - box on line 39b checked
                                             (or (cell-value ::spouse_itemizes_separately) (cell-value ::dual_status_alien))
                                             0
                                   ; All others
                                             true
                                             (condp = (cell-value ::filing_status)
                                               SINGLE 6300
                                               MARRIED_FILING_SEPARATELY 6300
                                               MARRIED_FILING_JOINTLY 12600
                                               QUALIFYING_WIDOW_WIDOWER 12600
                                               HEAD_OF_HOUSEHOLD 9250)))
   (make-number-input-line ::itemized_deductions) ; TODO, schedule A
   (make-formula-line ::deductions (max (cell-value ::standard_deduction) (cell-value ::itemized_deductions))) ; TODO: "In most cases, your federal income tax will be less if you take the larger of your itemized  deductions  or  standard  deduction." Should this be surfaced as a choice in case the lesser deduction makes more sense?
   (make-formula-line ::agi_minus_deductions (- (cell-value ::agi) (cell-value ::deductions)))
   (make-formula-line ::exemptions_ceiling (condp = (cell-value ::filing_status)
                                             SINGLE 258250
                                             MARRIED_FILING_JOINTLY 309900
                                             QUALIFYING_WIDOW_WIDOWER 309900
                                             MARRIED_FILING_SEPARATELY 154950
                                             HEAD_OF_HOUSEHOLD 284050))
   (make-formula-line ::exemptions (cond
                           ; Deduction for exemptions worksheet, line 1
                                     (<= (cell-value ::agi) (cell-value ::exemptions_ceiling))
                                     (* 4000 (cell-value ::exemptions_number))
                           ; Deduction for exemptions worksheet, line 5
                                     (> (- (cell-value ::agi) (cell-value ::exemptions_ceiling))
                                        (if (= (cell-value ::filing_status) MARRIED_FILING_SEPARATELY)
                                          61250
                                          122500))
                                     0
                           ; Deduction for exemptions worksheet, line 9
                                     true
                                     (-
                                      (* 4000 (cell-value ::exemptions_number))
                                      (* 4000 (cell-value ::exemptions_number) 0.02
                                         (ceil (/ (- (cell-value ::agi) (cell-value ::exemptions_ceiling))
                                                  (if (= (cell-value ::filing_status) MARRIED_FILING_SEPARATELY)
                                                    1250
                                                    2500)))))))
   (make-formula-line ::taxable_income (max 0 (- (cell-value ::agi_minus_deductions) (cell-value ::exemptions))))
   (make-formula-line ::tax (condp = (cell-value ::filing_status)
                              SINGLE
                              (condp > (cell-value ::taxable_income)
                                9225 (* 0.1 (cell-value ::taxable_income))
                                37450 (+ 922.50 (* 0.15 (- (cell-value ::taxable_income) 9225)))
                                90750 (+ 5156.25 (* 0.25 (- (cell-value ::taxable_income) 37450)))
                                189300 (+ 18481.25 (* 0.28 (- (cell-value ::taxable_income) 90750)))
                                411500 (+ 46075.25 (* 0.33 (- (cell-value ::taxable_income) 189300)))
                                413200 (+ 119401.25 (* 0.35 (- (cell-value ::taxable_income) 411500)))
                                (+ 119996.25 (* 0.396 (- (cell-value ::taxable_income) 413200))))
                              MARRIED_FILING_JOINTLY
                              (condp > (cell-value ::taxable_income)
                                18450 (* 0.1 (cell-value ::taxable_income))
                                74900 (+ 1845 (* 0.15 (- (cell-value ::taxable_income) 18450)))
                                151200 (+ 10312.50 (* 0.25 (- (cell-value ::taxable_income) 74900)))
                                230450 (+ 29387.50 (* 0.28 (- (cell-value ::taxable_income) 151200)))
                                411500 (+ 51577.50 (* 0.33 (- (cell-value ::taxable_income) 230450)))
                                464850 (+ 111324 (* 0.35 (- (cell-value ::taxable_income) 411500)))
                                (+ 129996.50 (* 0.396 (- (cell-value ::taxable_income) 464850))))
                              QUALIFYING_WIDOW_WIDOWER
                              (condp > (cell-value ::taxable_income)
                                18450 (* 0.1 (cell-value ::taxable_income))
                                74900 (+ 1845 (* 0.15 (- (cell-value ::taxable_income) 18450)))
                                151200 (+ 10312.50 (* 0.25 (- (cell-value ::taxable_income) 74900)))
                                230450 (+ 29387.50 (* 0.28 (- (cell-value ::taxable_income) 151200)))
                                411500 (+ 51577.50 (* 0.33 (- (cell-value ::taxable_income) 230450)))
                                464850 (+ 111324 (* 0.35 (- (cell-value ::taxable_income) 411500)))
                                (+ 129996.50 (* 0.396 (- (cell-value ::taxable_income) 464850))))
                              MARRIED_FILING_SEPARATELY
                              (condp > (cell-value ::taxable_income)
                                9225 (* 0.1 (cell-value ::taxable_income))
                                37450 (+ 922.50 (* 0.15 (- (cell-value ::taxable_income) 9225)))
                                75600 (+ 5156.25 (* 0.25 (- (cell-value ::taxable_income) 37450)))
                                115225 (+ 14693.75 (* 0.28 (- (cell-value ::taxable_income) 75600)))
                                205750 (+ 25788.75 (* 0.33 (- (cell-value ::taxable_income) 115225)))
                                232425 (+ 55662 (* 0.35 (- (cell-value ::taxable_income) 205750)))
                                (+ 64998.25 (* 0.396 (- (cell-value ::taxable_income) 232425))))
                              HEAD_OF_HOUSEHOLD
                              (condp > (cell-value ::taxable_income)
                                13150 (* 0.1 (cell-value ::taxable_income))
                                50200 (+ 1315 (* 0.15 (- (cell-value ::taxable_income) 13150)))
                                129600 (+ 6872.50 (* 0.25 (- (cell-value ::taxable_income) 50200)))
                                209850 (+ 26722.50 (* 0.28 (- (cell-value ::taxable_income) 129600)))
                                411500 (+ 49192.50 (* 0.33 (- (cell-value ::taxable_income) 209850)))
                                439000 (+ 115737 (* 0.35 (- (cell-value ::taxable_income) 411500)))
                                (+ 125362 (* 0.396 (- (cell-value ::taxable_income) 439000))))))
  ; TODO: Form 8814, Form 4972, section 962 election, Form 8863, Form 8621 taxes
   (make-number-input-line ::alternative_minimum_tax) ; TODO
   (make-number-input-line ::premium_credit_repayment) ; TODO
   (make-formula-line ::pretotal_tax (+ (cell-value ::tax)
                                        (cell-value ::alternative_minimum_tax)
                                        (cell-value ::premium_credit_repayment)))
   (make-number-input-line ::foreign_tax_credit) ; TODO
   (make-number-input-line ::child_dependent_care_credit) ; TODO
   (make-number-input-line ::education_credits) ; TODO
   (make-number-input-line ::retirement_savings_contributions_credit) ; TODO
   (make-number-input-line ::child_tax_credit) ; TODO
   (make-number-input-line ::residential_energy_credits) ; TODO
   (make-number-input-line ::other_credits) ; TODO
   (make-formula-line ::total_credits (+ (cell-value ::foreign_tax_credit)
                                         (cell-value ::child_dependent_care_credit)
                                         (cell-value ::education_credits)
                                         (cell-value ::retirement_savings_contributions_credit)
                                         (cell-value ::child_tax_credit)
                                         (cell-value ::residential_energy_credits)
                                         (cell-value ::other_credits)))
   (make-formula-line ::tax_minus_credits (max (- (cell-value ::pretotal_tax) (cell-value ::total_credits) (cell-value :tenforty.forms.ty2015.s8812/ctc)) 0))

   (make-number-input-line ::self_employment_tax) ; TODO
   (make-number-input-line ::unreported_social_security_medicare_tax) ; TODO
   (make-number-input-line ::additional_tax_retirement_plans) ; TODO
   (make-number-input-line ::household_employment_taxes) ; TODO
   (make-number-input-line ::first_time_homebuyer_credit_repayment) ; TODO
   (make-number-input-line ::health_care_individual_responsibility) ; TODO
   (make-number-input-line ::other_taxes) ; TODO
   (make-formula-line ::total_tax (+ (cell-value ::tax_minus_credits)
                                     (cell-value ::self_employment_tax)
                                     (cell-value ::unreported_social_security_medicare_tax)
                                     (cell-value ::additional_tax_retirement_plans)
                                     (cell-value ::household_employment_taxes)
                                     (cell-value ::first_time_homebuyer_credit_repayment)
                                     (cell-value ::health_care_individual_responsibility)
                                     (cell-value ::other_taxes)))

   (make-number-input-line ::federal_tax_withheld)
   (make-number-input-line ::estimated_tax_payments)
   (make-number-input-line ::earned_income_credit)
   (make-number-input-line ::additional_child_tax_credit)
   (make-number-input-line ::american_opportunity_credit)
   (make-number-input-line ::net_premium_tax_credit)
   (make-number-input-line ::payment_with_extension_request)
   (make-number-input-line ::excess_social_security_withheld)
   (make-number-input-line ::federal_fuel_tax_credit)
   (make-number-input-line ::other_credits_payments)
   (make-formula-line ::total_payments (+ (cell-value ::federal_tax_withheld)
                                          (cell-value ::estimated_tax_payments)
                                          (cell-value ::earned_income_credit)
                                          (cell-value ::additional_child_tax_credit)
                                          (cell-value ::american_opportunity_credit)
                                          (cell-value ::net_premium_tax_credit)
                                          (cell-value ::payment_with_extension_request)
                                          (cell-value ::excess_social_security_withheld)
                                          (cell-value ::federal_fuel_tax_credit)
                                          (cell-value ::other_credits_payments)))
   (make-formula-line ::refund (max 0 (- (cell-value ::total_payments) (cell-value ::total_tax))))
   (make-formula-line ::tax_owed (max 0 (- (cell-value ::total_tax) (cell-value ::total_payments))))])
