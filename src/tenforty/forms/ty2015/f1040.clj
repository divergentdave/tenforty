(ns tenforty.forms.ty2015.f1040
  (:use tenforty.core)
  (:require tenforty.forms.ty2015.s8812))

(defform
  (makeline ::tax_minus_credits (max (- (cell-value ::pretotal_tax) (cell-value ::total_credits) (cell-value :tenforty.forms.ty2015.s8812/ctc)) 0)))
