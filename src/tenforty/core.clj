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

(defn make-input-line [kw] (->InputLine kw))

(defrecord CodeInputLine [kw]
  LineMethods
  (get-keyword [self] (:kw self))
  (get-name [self] (name (:kw self)))
  (eval-line [self cell-value] (cell-value (:kw self)))
  (get-deps [self] #{}))

(defn make-code-input-line [kw] (->CodeInputLine kw))

(defrecord BooleanInputLine [kw]
  LineMethods
  (get-keyword [self] (:kw self))
  (get-name [self] (name (:kw self)))
  (eval-line [self cell-value] (cell-value (:kw self)))
  (get-deps [self] #{}))

(defn make-boolean-input-line [kw] (->BooleanInputLine kw))

(defrecord FormulaLine [kw fn deps]
  LineMethods
  (get-keyword [self] (:kw self))
  (get-name [self] (name (:kw self)))
  (eval-line [self cell-value] ((:fn self) cell-value))
  (get-deps [self] (:deps self)))

(defmacro make-formula-line
  [kw expression]
  (list 'tenforty.core/->FormulaLine kw (list 'fn ['cell-value] expression) (data-dependencies expression)))

(defmacro defform
  [& args]
  (let [l (gensym "l")
        map_acc (gensym "map_acc")
        part (gensym "part")
        coll (gensym "coll")
        obj (gensym "obj")]
    `(let [~l (list ~@args)]
       (reduce (fn [~coll ~part] (if (contains? ~coll (first ~part))
                                   (throw (IllegalArgumentException. (str "More than one group uses the keyword " (first ~part))))
                                   (conj ~coll (first ~part))))
               #{} (partition 2 ~l))
       (reduce (fn [~coll ~obj] (if (contains? ~coll (:kw ~obj))
                                  (throw (IllegalArgumentException. (str "More than one line uses the keyword " (:kw ~obj))))
                                  (conj ~coll (:kw ~obj))))
               #{} (apply concat (map second (partition 2 ~l))))

       (def ~'form (reduce (fn [~map_acc ~part]
                             (assoc
                              ~map_acc
                              (first ~part)
                              (zipmap (map :kw (second ~part)) (second ~part))))
                           {}
                           (partition 2 ~l))))))

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

(defrecord TenfortyContext [lines situation cache])

(defn make-context
  [lines situation]
  (->TenfortyContext lines situation (atom {})))

(defn calculate
  ([lines kw situation]
   (calculate (make-context lines situation) kw))
  ([context kw]
   (let [line (kw (:lines context))]
     (cond
       (instance? FormulaLine line)
       (if-let [entry (find @(:cache context) kw)]
         (val entry)
         (let [retval (eval-line line #(calculate context %))]
           (swap! (:cache context) assoc kw retval)
           retval))
       (or (instance? InputLine line)
           (instance? CodeInputLine line)
           (instance? BooleanInputLine line))
       (let [value (lookup (:situation context) kw)]
         (if (nil? value)
           (throw (Exception. (str "Tax situation has no value for " kw)))
           value))))))
