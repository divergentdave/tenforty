(ns tenforty.line-deps-test
  (:require [clojure.test :refer :all]
            [tenforty.core :refer :all]
            [tenforty.forms.ty2015]))

(defn check-deps
  [universe line]
  (let [deps (.get-deps line)
        contains (partial contains? universe)]
    (dorun (map #(is (contains %) (str %)) deps))))

(let [forms tenforty.forms.ty2015/forms
      universe (apply merge (vals forms))]
  (deftest line-deps
    (dorun (map #(testing (str "Check that lines are defined for all dependencies in " (key %))
                   (dorun (map (partial check-deps universe)
                               (vals (val %)))))
                (seq forms)))))
