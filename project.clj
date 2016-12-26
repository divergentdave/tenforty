(defproject tenforty "0.1.0-SNAPSHOT"
  :description "Tools for analyzing U.S. taxes"
  :url "https://github.com/divergentdave/tenforty"
  :license {:name "GNU GPL v2"
            :url "https://www.gnu.org/licenses/gpl-2.0.en.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]]
  :profiles {:dev {:plugins [[lein-cljfmt "0.5.6"]]}})
