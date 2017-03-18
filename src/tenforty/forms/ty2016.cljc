(ns tenforty.forms.ty2016
  (:require [tenforty.core :refer [merge-subgraphs]]
            [tenforty.forms.ty2016.f1040]
            [tenforty.forms.ty2016.w2]
            [tenforty.forms.ty2016.w2g]
            [tenforty.forms.ty2016.f1099r]
            [tenforty.forms.ty2016.ssa1099]
            [tenforty.forms.ty2016.rrb1099]
            [tenforty.forms.ty2016.f2441]
            [tenforty.forms.ty2016.s8812]
            [tenforty.forms.ty2016.f4137]
            [tenforty.forms.ty2016.f8959]
            [tenforty.forms.ty2016.f8919]))

(def forms (merge-subgraphs tenforty.forms.ty2016.f1040/form
                            tenforty.forms.ty2016.w2/form
                            tenforty.forms.ty2016.w2g/form
                            tenforty.forms.ty2016.f1099r/form
                            tenforty.forms.ty2016.ssa1099/form
                            tenforty.forms.ty2016.rrb1099/form
                            tenforty.forms.ty2016.f2441/form
                            tenforty.forms.ty2016.s8812/form
                            tenforty.forms.ty2016.f4137/form
                            tenforty.forms.ty2016.f8959/form
                            tenforty.forms.ty2016.f8919/form))
