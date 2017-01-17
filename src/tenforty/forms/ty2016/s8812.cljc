(ns tenforty.forms.ty2016.s8812
  (:require [tenforty.core :refer [defform
                                   make-number-input-line]]))

(defform
  [nil #{}]
  [(make-number-input-line ::ctc)]) ; TODO
