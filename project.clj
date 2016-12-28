(defproject tenforty "0.1.0-SNAPSHOT"
  :description "Tools for analyzing U.S. taxes"
  :url "https://github.com/divergentdave/tenforty"
  :license {:name "GNU GPL v2"
            :url "https://www.gnu.org/licenses/gpl-2.0.en.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]]
  :plugins [[lein-cljsbuild "1.1.5"]]
  :cljsbuild {:builds [{:source-paths ["src"]
                        :compiler {:output-dir "out"
                                   :optimizations :none
                                   :source-map true
                                   :pretty-print true}}]}
  :profiles {:dev {:plugins [[lein-cljfmt "0.5.6"]]}})
