(ns tenforty.math
  (:require #? (:clj [clojure.math.numeric-tower])))

#? (:clj
    (defn ceil
      [number]
      (clojure.math.numeric-tower/ceil
       number)))

#? (:cljs
    (defn ceil
      [number]
      (.ceil js/Math number)))
