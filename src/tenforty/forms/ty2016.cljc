(ns tenforty.forms.ty2016
  (:require [tenforty.core :refer [merge-subgraphs]]
            [tenforty.forms.ty2016.f1040]
            [tenforty.forms.ty2016.w2]
            [tenforty.forms.ty2016.f2441]
            [tenforty.forms.ty2016.s8812]))

(def forms (merge-subgraphs tenforty.forms.ty2016.f1040/form
                            tenforty.forms.ty2016.w2/form
                            tenforty.forms.ty2016.f2441/form
                            tenforty.forms.ty2016.s8812/form))
