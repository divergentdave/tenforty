(ns tenforty.core
  (:require [clojure.walk :refer [postwalk]]
            [clojure.set]))

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

(defrecord NumberInputLine [kw group]
  LineMethods
  (get-keyword [self] (:kw self))
  (get-name [self] (name (:kw self)))
  (eval-line [self cell-value] (cell-value (:kw self)))
  (get-deps [self] #{})
  (get-group [self] (:group self)))

(defn make-number-input-line
  ([kw] (->NumberInputLine kw nil))
  ([kw group] (->NumberInputLine kw group)))

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

(defrecord FormSubgraph [lines groups])

(defn merge-subgraphs
  [& subgraphs]
  (->FormSubgraph (apply merge (map :lines subgraphs))
                  (apply merge-with clojure.set/union (map :groups subgraphs))))

(defmacro make-form-subgraph
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
         (->FormSubgraph ~lines ~groups)))))

(defmacro defform
  [& args]
  `(def ~'form (make-form-subgraph ~@args)))

(defprotocol GroupValues
  (lookup-value [self kw])
  (lookup-group [self kw]))

(defrecord ZeroTaxSituation []
  GroupValues
  (lookup-value [self kw] 0)
  (lookup-group [self kw] [self]))

(defrecord MapTaxSituation [values groups]
  GroupValues
  (lookup-value [self kw] (kw (:values self)))
  (lookup-group [self kw] (kw (:groups self))))

(declare ->EdnTaxSituation)
(defrecord EdnTaxSituation [object]
  GroupValues
  (lookup-value [self kw] (kw (:values (:object self))))
  (lookup-group [self kw] (map ->EdnTaxSituation (kw (:groups (:object self))))))

(defrecord TenfortyContext [form-subgraph situation group parent-context value-cache child-context-cache])

(defn make-context
  ([form-subgraph situation]
   (make-context form-subgraph situation nil))
  ([form-subgraph situation group]
   (make-context form-subgraph situation group nil))
  ([form-subgraph situation group parent-context]
   (->TenfortyContext form-subgraph situation group parent-context (atom {}) (atom {}))))

(defn- find-group-parent-contexts
  [group-kw context]
  (let [;line (line-kw (:lines (:form-subgraph context)))
        ;group-kw (:group line)
        parent-context (:parent-context context)]
    (if (= group-kw (:group context))
      context
      (if parent-context
        (find-group-parent-contexts group-kw parent-context)
        nil))))

(defn- throw-portable
  [message]
  #? (:clj
      (throw
       (Exception. message)))
  #? (:cljs
      (throw (js/Error. message))))

(defn input-line? [line]
  (or (instance? NumberInputLine line)
      (instance? CodeInputLine line)
      (instance? BooleanInputLine line)))

(declare calculate)

(defn- calculate-context
  [context kw]
  (let [line-entry (find (:lines (:form-subgraph context)) kw)
        line (val line-entry)]
    (cond
      (instance? FormulaLine line)
      (if-let [cache-entry (find @(:value-cache context) kw)]
        (val cache-entry)
        (let [retval (eval-line line #(calculate context %))]
          (swap! (:value-cache context) assoc kw retval)
          retval))
      (input-line? line)
      (let [value (lookup-value (:situation context) kw)]
        (if (nil? value)
          (throw-portable (str "Tax situation has no value for " kw " in group " (:group context)))
          value)))))

(defn calculate
  ([form-subgraph kw situation]
   (calculate (make-context form-subgraph situation) kw))
  ([form-subgraph kw situation group]
   (calculate (make-context form-subgraph situation group) kw))
  ([context kw]
   (let [group-kw (:group (kw (:lines (:form-subgraph context))))]
     (if-let [parent-or-self-context (find-group-parent-contexts group-kw context)]
       (calculate-context parent-or-self-context kw)
       (let [child-group-kws (get (:groups (:form-subgraph context)) (:group context))]
         (if (contains? child-group-kws group-kw)
           (if-let [cache-entry (find @(:child-context-cache context) group-kw)]
             (map #(calculate-context % kw) (val cache-entry))
             (let [new-contexts (map #(make-context (:form-subgraph context) % group-kw context)
                                     (lookup-group (:situation context) group-kw))]
               (swap! (:child-context-cache context) assoc group-kw new-contexts)
               (map #(calculate-context % kw) new-contexts)))
           (throw-portable (str "Line " kw " in group " group-kw " was referenced from group " (:group context) " but " group-kw " is not a direct child of " (:group context)))))))))

(defn- recursive-deps
  [lines kw]
  (let [deps (get-deps (kw lines))]
    (apply clojure.set/union deps (map (partial recursive-deps lines) deps))))

(defn reverse-deps [form-subgraph]
  (let [lines (:lines form-subgraph)
        line-kws (keys lines)]
    (apply merge-with clojure.set/union
           (zipmap line-kws (repeat #{}))
           (map
            (fn [line-kw]
              (let [deps (seq (recursive-deps lines line-kw))]
                (apply merge-with clojure.set/union (map #(sorted-map % #{line-kw}) deps))))
            line-kws))))
