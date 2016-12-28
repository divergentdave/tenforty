(ns tenforty.forms.ty2015
  (:require [tenforty.core :refer [merge-subgraphs]]
            [tenforty.forms.ty2015.f1040]
            [tenforty.forms.ty2015.s8812]))

(def forms (merge-subgraphs tenforty.forms.ty2015.f1040/form
                            tenforty.forms.ty2015.s8812/form))
