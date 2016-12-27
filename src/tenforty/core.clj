(ns tenforty.core
  (:use clojure.walk)
  (:require clojure.set))

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
  (get-deps [self])
  (get-group [self]))

(defrecord InputLine [kw group]
  LineMethods
  (get-keyword [self] (:kw self))
  (get-name [self] (name (:kw self)))
  (eval-line [self cell-value] (cell-value (:kw self)))
  (get-deps [self] #{})
  (get-group [self] (:group self)))

(defn make-input-line
  ([kw] (->InputLine kw nil))
  ([kw group] (->InputLine kw group)))

(defrecord CodeInputLine [kw group]
  LineMethods
  (get-keyword [self] (:kw self))
  (get-name [self] (name (:kw self)))
  (eval-line [self cell-value] (cell-value (:kw self)))
  (get-deps [self] #{})
  (get-group [self] (:group self)))

(defn make-code-input-line
  ([kw] (->CodeInputLine kw nil))
  ([kw group] (->CodeInputLine kw group)))

(defrecord BooleanInputLine [kw group]
  LineMethods
  (get-keyword [self] (:kw self))
  (get-name [self] (name (:kw self)))
  (eval-line [self cell-value] (cell-value (:kw self)))
  (get-deps [self] #{}))

(defn make-boolean-input-line
  ([kw] (->BooleanInputLine kw nil))
  ([kw group] (->BooleanInputLine kw group)))

(defrecord FormulaLine [kw group fn deps]
  LineMethods
  (get-keyword [self] (:kw self))
  (get-name [self] (name (:kw self)))
  (eval-line [self cell-value] ((:fn self) cell-value))
  (get-deps [self] (:deps self))
  (get-group [self] (:group self)))

(defmacro make-formula-line
  ([kw expression]
   `(make-formula-line ~kw nil ~expression))
  ([kw group expression]
   (list 'tenforty.core/->FormulaLine kw group (list 'fn ['cell-value] expression) (data-dependencies expression))))

(defmacro defform
  [& args]
  (let [l (gensym "l")
        map_acc (gensym "map_acc")
        part (gensym "part")
        coll (gensym "coll")
        obj (gensym "obj")
        lines (gensym "lines")
        groups (gensym "groups")]
    `(let [~l (list ~@args)]
       (reduce (fn [~coll ~obj] (if (contains? ~coll (:kw ~obj))
                                  (throw (IllegalArgumentException. (str "More than one line uses the keyword " (:kw ~obj))))
                                  (conj ~coll (:kw ~obj))))
               #{} (apply concat (map second (partition 2 ~l))))

       (let [~lines (reduce (fn [~map_acc ~part]
                              (merge
                               ~map_acc
                               (zipmap (map :kw (second ~part))
                                       (map #(assoc % :group (first (first ~part)))
                                            (second ~part)))))
                            {}
                            (partition 2 ~l))
             ~groups (apply merge-with clojure.set/union (sorted-map)
                            (map #(sorted-map (first (first %)) (second (first %)))
                                 (partition 2 ~l)))]
         (def ~'form (->FormSubgraph ~lines ~groups))))))

(defprotocol GroupValues
  (lookup-value [self kw])
  (lookup-group [self kw]))

(defrecord ZeroTaxSituation []
  GroupValues
  (lookup-value [self kw] 0)
  (lookup-group [self kw] self))

(defrecord MapTaxSituation [values groups]
  GroupValues
  (lookup-value [self kw] (kw (:values self)))
  (lookup-group [self kw] (kw (:groups self))))

(defrecord FormSubgraph [lines groups])

(defn merge-subgraphs
  [& subgraphs]
  (->FormSubgraph (apply merge (map :lines subgraphs))
                  (apply merge-with clojure.set/union (map :groups subgraphs))))

(defrecord TenfortyContext [form-subgraph situation group parent-context value-cache])

(defn make-context
  ([form-subgraph situation]
   (make-context form-subgraph situation nil))
  ([form-subgraph situation group]
   (make-context form-subgraph situation group nil))
  ([form-subgraph situation group parent-context]
   (->TenfortyContext form-subgraph situation group parent-context (atom {}))))

(defn calculate
  ([form-subgraph kw situation]
   (calculate (make-context form-subgraph situation) kw))
  ([form-subgraph kw situation group]
   (calculate (make-context form-subgraph situation group) kw))
  ([context kw]
   (let [line-entry (find (:lines (:form-subgraph context)) kw)
         line (val line-entry)]
     (cond
       (instance? FormulaLine line)
       (if-let [cache-entry (find @(:value-cache context) kw)]
         (val cache-entry)
         (let [retval (eval-line line #(calculate context %))]
           (swap! (:value-cache context) assoc kw retval)
           retval))
       (or (instance? InputLine line)
           (instance? CodeInputLine line)
           (instance? BooleanInputLine line))
       (let [value (lookup-value (:situation context) kw)]
         (if (nil? value)
           (throw (Exception. (str "Tax situation has no value for " kw)))
           value))))))
