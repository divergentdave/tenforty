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
  (->InputLine ::sch_e_income) ; TODO
  (->InputLine ::farm_income_loss) ; TODO
  (->InputLine ::unemployment_compensation) ; TODO
  (->InputLine ::social_security_benefits) ; TODO
  (->InputLine ::social_security_benefits_taxable) ; TODO
  (->InputLine ::other_income) ; TODO

  (makeline ::tax_minus_credits (max (- (cell-value ::pretotal_tax) (cell-value ::total_credits) (cell-value :tenforty.forms.ty2015.s8812/ctc)) 0)))
