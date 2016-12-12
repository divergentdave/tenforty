(ns tenforty.core
  (:use clojure.walk))

(defrecord data-dependency [kw])

(defn data-dependencies [l]
  (let [transformed
        (postwalk
         #(let [retval
                (if (seq? %)
                  (if (= (first %) 'cell-value)
                    (data-dependency. (second %))
                    %)
                  %)]
            retval)
         (seq [l]))
        filtered
        (postwalk
         #(let [retval
                (if (seq? %)
                  %
                  (if (instance? data-dependency %)
                    (:kw %)
                    ()))]
            retval)
         transformed)
        result
        (postwalk
         (fn [form] (if (seq? form)
                      (let [listified
                            (map (fn [x] (if (seq? x)
                                           x
                                           (seq [x]))) form)
                            retval
                            (apply concat listified)]
                        retval)
                      form))
         filtered)]
    result))

(defrecord line [kw fn deps])

(defmacro makeline
  [kw expression]
  (list 'tenforty.core/->line kw (list 'fn ['cell-value] expression) (cons 'list (data-dependencies expression))))
