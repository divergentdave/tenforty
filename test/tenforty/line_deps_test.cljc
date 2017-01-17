(ns tenforty.line-deps-test
  (:require [clojure.test :refer [deftest
                                  testing
                                  is]
             :include-macros true]
            [tenforty.core :refer [get-deps]]
            [tenforty.forms.ty2015]
            [tenforty.forms.ty2016]))

(defn check-deps
  [universe line]
  (let [deps (get-deps line)
        contains (partial contains? universe)]
    (dorun (map #(is (contains %) (str %)) deps))))

(let [forms tenforty.forms.ty2015/forms
      universe (:lines forms)]
  (deftest line-deps-2015
    (testing "Check that lines are defined for all dependencies"
      (dorun (map (partial check-deps universe)
                  (vals universe))))))

(let [forms tenforty.forms.ty2016/forms
      universe (:lines forms)]
  (deftest line-deps-2016
    (testing "Check that lines are defined for all dependencies"
      (dorun (map (partial check-deps universe)
                  (vals universe))))))
