(ns tenforty.math
  (:require [clojure.math.numeric-tower]))

(defn ceil
  [number]
  (clojure.math.numeric-tower/ceil number))
