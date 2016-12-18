(ns tenforty.line-deps-test
  (:require [clojure.test :refer :all]
            [tenforty.core :refer :all]
            [tenforty.forms.ty2015.f1040]))

(defn check-deps
  [universe line]
  (let [deps (.get-deps line)
        contains (partial contains? universe)]
    (dorun (map #(is (contains %) (str %)) deps))))

(let [universe (merge tenforty.forms.ty2015.f1040/form
                      tenforty.forms.ty2015.s8812/form)]
  (deftest f1040-deps
    (testing "Check that lines are defined for all dependencies in form 1040"
      (dorun (map (partial check-deps universe) (vals tenforty.forms.ty2015.f1040/form))))))
