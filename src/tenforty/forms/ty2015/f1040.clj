(ns tenforty.forms.ty2015.f1040
  (:use tenforty.core)
  (:require tenforty.forms.ty2015.s8812))

; Filing status codes
(def ^:const SINGLE 1)
(def ^:const MARRIED_FILING_JOINTLY 2)
(def ^:const MARRIED_FILING_SEPARATELY 3)
(def ^:const HEAD_OF_HOUSEHOLD 4)
(def ^:const QUALIFYING_WIDOW_WIDOWER 5)

(defform
  (->InputLine ::pretotal_tax) ; TODO
  (->InputLine ::total_credits) ; TODO

  (->CodeInputLine ::filing_status) ; IRS1040/IndividualReturnFilingStatusCd/text()
  (->BooleanInputLine ::exemption_self)
  (->BooleanInputLine ::exemption_spouse)
  (->InputLine ::dependents)
  (makeline ::exemptions (+ (if (cell-value ::exemption_self) 1 0)
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
  (makeline ::standard_deduction (if (if (= (cell-value ::filing_status) MARRIED_FILING_JOINTLY)
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
                                   (if (> (cell-value ::senior_blind_total) 0)
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
                                     (if (or (cell-value ::spouse_itemizes_separately) (cell-value ::dual_status_alien))
                                       0
                                       (case (cell-value ::filing_status)
                                         SINGLE 6300
                                         MARRIED_FILING_SEPAREATELY 6300
                                         MARRIED_FILING_JOINTLY 12600
                                         QUALIFYING_WIDOW_WIDOWER 12600
                                         HEAD_OF_HOUSEHOLD 9250)))))
  (->InputLine ::itemized_deductions) ; TODO, schedule A
  (makeline ::deductions (max (cell-value ::standard_deduction) (cell-value ::itemized_deductions))) ; TODO: "In most cases, your federal income tax will be less if you take the larger of your itemized  deductions  or  standard  deduction." Should this be surfaced as a choice in case the lesser deduction makes more sense?

  (makeline ::tax_minus_credits (max (- (cell-value ::pretotal_tax) (cell-value ::total_credits) (cell-value :tenforty.forms.ty2015.s8812/ctc)) 0)))
