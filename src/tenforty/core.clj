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
         (list l))
        filtered
        (postwalk
         #(let [retval
                (if (seq? %)
                  %
                  (if (instance? data-dependency %)
                    (:kw %)
                    (list)))]
            retval)
         transformed)
        result
        (postwalk
         (fn [form] (if (seq? form)
                      (let [listified
                            (map (fn [x] (if (seq? x)
                                           x
                                           (list x))) form)
                            retval
                            (apply concat listified)]
                        retval)
                      form))
         filtered)]
    result))

(defprotocol LineMethods
  (get-keyword [self])
  (get-name [self])
  (eval-line [self cell-value])
  (get-deps [self]))

(defrecord InputLine [kw]
  LineMethods
  (get-keyword [self] (:kw self))
  (get-name [self] (name (:kw self)))
  (eval-line [self cell-value] (cell-value (:kw self)))
  (get-deps [self] (list)))

(defrecord FormulaLine [kw fn deps]
  LineMethods
  (get-keyword [self] (:kw self))
  (get-name [self] (name (:kw self)))
  (eval-line [self cell-value] ((:fn self) cell-value))
  (get-deps [self] (:deps self)))

(defmacro makeline
  [kw expression]
  (list 'tenforty.core/->FormulaLine kw (list 'fn ['cell-value] expression) (cons 'list (data-dependencies expression))))
