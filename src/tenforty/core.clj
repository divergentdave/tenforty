(ns tenforty.core
  (:use clojure.walk))

(defrecord data-dependency [kw])

(defn data-dependencies [l]
  (let [transformed
        (postwalk
         #(let [retval
                (cond
                  (seq? %)
                  (if (= (first %) 'cell-value)
                    (data-dependency. (second %))
                    %)
                  (vector? %)
                  (seq %)
                  true
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
    (set result)))

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
  (get-deps [self] #{}))

(defrecord CodeInputLine [kw]
  LineMethods
  (get-keyword [self] (:kw self))
  (get-name [self] (name (:kw self)))
  (eval-line [self cell-value] (cell-value (:kw self)))
  (get-deps [self] #{}))

(defrecord BooleanInputLine [kw]
  LineMethods
  (get-keyword [self] (:kw self))
  (get-name [self] (name (:kw self)))
  (eval-line [self cell-value] (cell-value (:kw self)))
  (get-deps [self] #{}))

(defrecord FormulaLine [kw fn deps]
  LineMethods
  (get-keyword [self] (:kw self))
  (get-name [self] (name (:kw self)))
  (eval-line [self cell-value] ((:fn self) cell-value))
  (get-deps [self] (:deps self)))

(defprotocol TaxSituation
  (lookup [self kw]))

(defrecord ZeroTaxSituation []
  TaxSituation
  (lookup [self kw] 0))

(defrecord MapTaxSituation [data]
  TaxSituation
  (lookup [self kw] (kw (:data self))))

(defrecord CompositeTaxSituation [situations]
  TaxSituation
  (lookup [self kw] (first (keep #(lookup % kw) (:situations self)))))

(defmacro makeline
  [kw expression]
  (list 'tenforty.core/->FormulaLine kw (list 'fn ['cell-value] expression) (data-dependencies expression)))

(defmacro defform
  [& lines]
  (let [sym (gensym)
        coll (gensym "coll")
        obj (gensym "obj")]
    `(let [~sym (list ~@lines)]
       (reduce (fn [~coll ~obj] (if (contains? ~coll (:kw ~obj))
                                  (throw (IllegalArgumentException. (str "More than one line uses the keyword " (:kw ~obj))))
                                  (conj ~coll (:kw ~obj)))) #{} ~sym)
       (def ~'form (zipmap (map :kw ~sym) ~sym)))))
