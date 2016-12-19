(ns tenforty.forms.ty2015.f1040
  (:require [clojure.math.numeric-tower :refer [ceil]])
  (:use tenforty.core)
  (:require tenforty.forms.ty2015.s8812))

; Filing status codes
(def ^:const SINGLE 1)
(def ^:const MARRIED_FILING_JOINTLY 2)
(def ^:const MARRIED_FILING_SEPARATELY 3)
(def ^:const HEAD_OF_HOUSEHOLD 4)
(def ^:const QUALIFYING_WIDOW_WIDOWER 5)

(defform
  (->CodeInputLine ::filing_status) ; IRS1040/IndividualReturnFilingStatusCd/text()
  (->BooleanInputLine ::exemption_self)
  (->BooleanInputLine ::exemption_spouse)
  (->InputLine ::dependents)
  (makeline ::exemptions_number (+ (if (cell-value ::exemption_self) 1 0)
                                   (if (cell-value ::exemption_spouse) 1 0)
                                   (cell-value ::dependents)))

  (->InputLine ::wages) ; TODO
  (->InputLine ::taxable_interest) ; TODO
  (->InputLine ::tax_exempt_interest) ; TODO
  (->InputLine ::ordinary_dividends) ; TODO
  (->InputLine ::qualified_dividends) ; TODO

  (->InputLine ::last_year_refund)
  (->InputLine ::last_year_itemized_deductions)
  (->CodeInputLine ::last_year_filing_status)
  (->BooleanInputLine ::last_year_senior)
  (->BooleanInputLine ::last_year_spouse_senior)
  (->BooleanInputLine ::last_year_blind)
  (->BooleanInputLine ::last_year_spouse_blind)
  (makeline ::taxable_tax_refunds ; TODO: The instructions are full of exceptions that aren't captured here yet, see publication 525 for complete details
            (min (cell-value ::last_year_refund)
                 (max (- (cell-value ::last_year_itemized_deductions)
                         (case (cell-value ::last_year_filing_status)
                           SINGLE 6200
                           MARRIED_FILING_SEPARATELY 6200
                           MARRIED_FILING_JOINTLY 12400
                           QUALIFYING_WIDOW_WIDOWER 12400
                           HEAD_OF_HOUSEHOLD 9100)
                         (* (+ (if (cell-value ::last_year_senior) 1 0)
                               (if (cell-value ::last_year_spouse_senior) 1 0)
                               (if (cell-value ::last_year_blind) 1 0)
                               (if (cell-value ::last_year_spouse_blind) 1 0))
                            (case (cell-value ::last_year_filing_status)
                              MARRIED_FILING_SEPARATELY 1200
                              MARRIED_FILING_JOINTLY 1200
                              QUALIFYING_WIDOW_WIDOWER 1200
                              SINGLE 1550
                              HEAD_OF_HOUSEHOLD 1550))) 0))) ; IRS1040/StateLocalIncomeTaxRefundAmt/text()

  (->InputLine ::alimony_received) ; TODO
  (->InputLine ::business_income_loss) ; TODO
  (->InputLine ::capital_gain_loss) ; TODO
  (->InputLine ::other_gain_loss) ; TODO
  (->InputLine ::ira_distributions) ; TODO
  (->InputLine ::ira_distributions_taxable) ; TODO
  (->InputLine ::pensions_annuities) ; TODO
  (->InputLine ::pensions_annuities_taxable) ; TODO
  (->InputLine ::schedule_e_income) ; TODO
  (->InputLine ::farm_income_loss) ; TODO
  (->InputLine ::unemployment_compensation) ; TODO
  (->InputLine ::social_security_benefits) ; TODO
  (->InputLine ::social_security_benefits_taxable) ; TODO
  (->InputLine ::other_income) ; TODO

  (makeline ::total_income (+ (cell-value ::wages)
                              (cell-value ::taxable_interest)
                              (cell-value ::ordinary_dividends)
                              (cell-value ::taxable_tax_refunds)
                              (cell-value ::alimony_received)
                              (cell-value ::business_income_loss)
                              (cell-value ::capital_gain_loss)
                              (cell-value ::ira_distributions_taxable)
                              (cell-value ::pensions_annuities_taxable)
                              (cell-value ::schedule_e_income)
                              (cell-value ::farm_income_loss)
                              (cell-value ::unemployment_compensation)
                              (cell-value ::social_security_benefits_taxable)
                              (cell-value ::other_income)))
  (makeline ::magi_total_income (+ (cell-value ::wages)
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

  (->InputLine ::educator_expenses) ; TODO
  (->InputLine ::expenses_2106) ; TODO
  (->InputLine ::hsa_deduction) ; TODO
  (->InputLine ::moving_expenses) ; TODO
  (->InputLine ::self_employment_tax_deductible) ; TODO
  (->InputLine ::self_employed_pension_deduction) ; TODO
  (->InputLine ::self_employed_health_insurance_deduction) ; TODO
  (->InputLine ::early_withdrawl_penalty) ; TODO
  (->InputLine ::alimony_paid) ; TODO
  (->InputLine ::ira_deduction) ; TODO
  (->InputLine ::student_loan_interest_deduction) ; TODO
  (->InputLine ::tuition_fees_deduction) ; TODO
  (->InputLine ::domestic_production_activities_deduction) ; TODO
  (makeline ::agi_deductions (+ (cell-value ::educator_expenses)
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
  (makeline ::agi (- (cell-value ::total_income) (cell-value ::agi_deductions)))

  (->BooleanInputLine ::senior)
  (->BooleanInputLine ::spouse_senior)
  (->BooleanInputLine ::blind)
  (->BooleanInputLine ::spouse_blind)
  (->BooleanInputLine ::spouse_itemizes_separately)
  (->BooleanInputLine ::dual_status_alien)
  (makeline ::senior_blind_total (+ (if (cell-value ::senior) 1 0)
                                    (if (cell-value ::spouse_senior) 1 0)
                                    (if (cell-value ::blind) 1 0)
                                    (if (cell-value ::spouse_blind) 1 0)))
  (makeline ::earned_income (+ (cell-value ::wages)
                               (cell-value ::business_income_loss)
                               (cell-value ::farm_income_loss)
                               (- (cell-value ::self_employment_tax_deductible))))
  (makeline ::standard_deduction (cond
                                   ; Exception 1 - dependent
                                   (if (= (cell-value ::filing_status) MARRIED_FILING_JOINTLY)
                                     (not (and (cell-value ::exemption_self) (cell-value ::exemption_spouse)))
                                     (not (cell-value ::exemption_self)))
                                   (+ ; Standard Deduction Worksheet for Dependents
                                    (min (+ 350 (max 700 (cell-value ::earned_income)))
                                         (case (cell-value ::filing_status)
                                           SINGLE 6300
                                           MARRIED_FILING_SEPARATELY 6300
                                           MARRIED_FILING_JOINTLY 12600
                                           QUALIFYING_WIDOW_WIDOWER 12600 ; not on worksheet, but inferred
                                           HEAD_OF_HOUSEHOLD 9250))
                                    (* (cell-value ::senior_blind_total)
                                       (case (cell-value ::filing_status)
                                         MARRIED_FILING_JOINTLY 1250
                                         QUALIFYING_WIDOW_WIDOWER 1250
                                         MARRIED_FILING_SEPARATELY 1250
                                         SINGLE 1550
                                         HEAD_OF_HOUSEHOLD 1550)))
                                   ; Exception 2 - box on line 39a checked
                                   (> (cell-value ::senior_blind_total) 0)
                                   (case (cell-value ::filing_status)
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
                                   (case (cell-value ::filing_status)
                                     SINGLE 6300
                                     MARRIED_FILING_SEPAREATELY 6300
                                     MARRIED_FILING_JOINTLY 12600
                                     QUALIFYING_WIDOW_WIDOWER 12600
                                     HEAD_OF_HOUSEHOLD 9250)))
  (->InputLine ::itemized_deductions) ; TODO, schedule A
  (makeline ::deductions (max (cell-value ::standard_deduction) (cell-value ::itemized_deductions))) ; TODO: "In most cases, your federal income tax will be less if you take the larger of your itemized  deductions  or  standard  deduction." Should this be surfaced as a choice in case the lesser deduction makes more sense?
  (makeline ::agi_minus_deductions (- (cell-value ::agi) (cell-value ::deductions)))
  (makeline ::exemptions_ceiling (case (cell-value ::filing_status)
                                   SINGLE 258250
                                   MARRIED_FILING_JOINTLY 309900
                                   QUALIFYING_WIDOW_WIDOWER 309900
                                   MARRIED_FILING_SEPARATELY 154950
                                   HEAD_OF_HOUSEHOLD 284050))
  (makeline ::exemptions (cond
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
  (makeline ::taxable_income (max 0 (- (cell-value ::agi_minus_deductions) (cell-value ::exemptions))))
  (makeline ::tax (case (cell-value ::filing_status)
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
  (->InputLine ::alternative_minimum_tax) ; TODO
  (->InputLine ::premium_credit_repayment) ; TODO
  (makeline ::pretotal_tax (+ (cell-value ::tax)
                              (cell-value ::alternative_minimum_tax)
                              (cell-value ::premium_credit_repayment)))
  (->InputLine ::foreign_tax_credit) ; TODO
  (->InputLine ::child_dependent_care_credit) ; TODO
  (->InputLine ::education_credits) ; TODO
  (->InputLine ::retirement_savings_contributions_credit) ; TODO
  (->InputLine ::child_tax_credit) ; TODO
  (->InputLine ::residential_energy_credits) ; TODO
  (->InputLine ::other_credits) ; TODO
  (makeline ::total_credits (+ (cell-value ::foreign_tax_credit)
                               (cell-value ::child_dependent_care_credit)
                               (cell-value ::education_credits)
                               (cell-value ::retirement_savings_contributions_credit)
                               (cell-value ::child_tax_credit)
                               (cell-value ::residential_energy_credits)
                               (cell-value ::other_credits)))
  (makeline ::tax_minus_credits (max (- (cell-value ::pretotal_tax) (cell-value ::total_credits) (cell-value :tenforty.forms.ty2015.s8812/ctc)) 0))

  (->InputLine ::self_employment_tax) ; TODO
  (->InputLine ::unreported_social_security_medicare_tax) ; TODO
  (->InputLine ::additional_tax_retirement_plans) ; TODO
  (->InputLine ::household_employment_taxes) ; TODO
  (->InputLine ::first_time_homebuyer_credit_repayment) ; TODO
  (->InputLine ::health_care_individual_responsibility) ; TODO
  (->InputLine ::other_taxes) ; TODO
  (makeline ::total_tax (+ (cell-value ::tax_minus_credits)
                           (cell-value ::self_employment_tax)
                           (cell-value ::unreported_social_security_medicare_tax)
                           (cell-value ::additional_tax_retirement_plans)
                           (cell-value ::household_employment_taxes)
                           (cell-value ::first_time_homebuyer_credit_repayment)
                           (cell-value ::health_care_individual_responsibility)
                           (cell-value ::other_taxes)))

  (->InputLine ::federal_tax_withheld)
  (->InputLine ::estimated_tax_payments)
  (->InputLine ::earned_income_credit)
  (->InputLine ::additional_child_tax_credit)
  (->InputLine ::american_opportunity_credit)
  (->InputLine ::net_premium_tax_credit)
  (->InputLine ::payment_with_extension_request)
  (->InputLine ::excess_social_security_withheld)
  (->InputLine ::federal_fuel_tax_credit)
  (->InputLine ::other_credits_payments)
  (makeline ::total_payments (+ (cell-value ::federal_tax_withheld)
                                (cell-value ::estimated_tax_payments)
                                (cell-value ::earned_income_credit)
                                (cell-value ::additional_child_tax_credit)
                                (cell-value ::american_opportunity_credit)
                                (cell-value ::net_premium_tax_credit)
                                (cell-value ::payment_with_extension_request)
                                (cell-value ::excess_social_security_withheld)
                                (cell-value ::federal_fuel_tax_credit)
                                (cell-value ::other_credits_payments)))
  (makeline ::refund (max 0 (- (cell-value ::total_payments) (cell-value ::total_tax))))
  (makeline ::tax_owed (max 0 (- (cell-value ::total_tax) (cell-value ::total_payments)))))
