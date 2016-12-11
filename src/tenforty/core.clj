(ns tenforty.core
  (:use clojure.walk))

(comment

  (defn cell-value [kwd] nil)

  (defn defline
    [name expression]
    nil)

  (defmacro defform
    [name year & lines]
    (map #(apply defline %) lines))) (defrecord data-dependency [kw])

(defn data-dependencies [l]
  (let [transformed
        (postwalk
         #(let [retval
                (if (seq? %)
                  (if (= (first %) 'cell-value)
                    (data-dependency. (second %))
                    %)
                  %)]
            (println "xform" % "=>" retval)
            retval)
         (seq [l]))]
    (println "transformed" transformed)
    (let [filtered
          (postwalk
           #(let [retval
                  (if (seq? %)
                    %
                    (if (instance? data-dependency %)
                      (:kw %)
                      ()))]
              (println "filter" % "=>" retval)
              retval)
           transformed)]
      (println "filtered" filtered)
      (let [result
            (postwalk
             (fn [form] (if (seq? form)
                          (let [listified
                                (map (fn [x] (if (seq? x)
                                               x
                                               (seq [x]))) form)
                                retval
                                (apply concat listified)]
                            (println "result" form "=>" retval)
                            retval)
                          form))
             filtered)]
        (println "result" result)
        result))))

(comment defn data-dependencies [l]
         (walk
          #(do
             (let [retval
                   (if (seq? %)
                     (if (= (first %) 'cell-value)
                       (rest %)
                       ())
                     ())]
               (println "inner" % "=>" retval)
               retval))
          #(do
             (let [retval
                   (apply concat %)]
               (println "outer" % "=>" retval)
               retval))
          (seq [l])))

(comment defn data-dependencies [l]
         (walk
          #(if (seq? %)
             (if (= (first %) 'cell-value)
               (rest %)
               ())
             ())
          #(apply concat %)
          l))

(comment defn data-dependencies [l]
         (walk identity #(if (= (first %) 'cell-value) % ()) l))

; want to end up with a mapping of form names to forms
; forms are mappings of line names/numbers to line thingys
; line thingy is a StructMap with name, sequence to run to get value
; use destructuring to parse objects
; macros and `/~ or functions and just ' quoting?
; probably macros because we can use list once with ~ and once for reflection
; -main to run stuff from
; double check directory structure/namespaces are correct for AOT .class generation
; there's a difference bewteen sequences in a macro and ASTs walked with tools.analyzer

; make different classes for different lines, inheritance etc.
; class for calculated cells, class for inputs
; subclasses for numeric inputs, numeric inputs that must be positive, etc.
; subclasses for enum inputs, yes/no inputs
; could have subclasses for string inputs for completeness, but they won't affect calculations, so skip
