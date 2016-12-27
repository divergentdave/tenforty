(ns tenforty.forms.ty2015
  (:use tenforty.core)
  (:require tenforty.forms.ty2015.f1040)
  (:require tenforty.forms.ty2015.s8812))

(def forms (merge-subgraphs tenforty.forms.ty2015.f1040/form
                            tenforty.forms.ty2015.s8812/form))
