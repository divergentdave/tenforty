(ns tenforty.test
  (:require [cljs.test :refer-macros [run-all-tests]]
            [tenforty.core-test]
            [tenforty.line-deps-test]
            [tenforty.forms-smoke-test]))

(enable-console-print!)

(defn ^:export run
  [callback]
  (defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
    (callback (cljs.test/successful? m)))
  (run-all-tests #"tenforty\..*-test"))
