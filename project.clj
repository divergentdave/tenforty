(defproject tenforty "0.1.0-SNAPSHOT"
  :description "Tools for analyzing U.S. taxes"
  :url "https://github.com/divergentdave/tenforty"
  :license {:name "GNU GPL v2"
            :url "https://www.gnu.org/licenses/gpl-2.0.en.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293"]
                 [org.clojure/math.numeric-tower "0.0.4"]]
  :plugins [[lein-cljsbuild "1.1.5"]]
  :cljsbuild {:builds {:dev {:source-paths ["src"]
                             :compiler {:output-to "out/cljs.js"
                                        :output-dir "out"
                                        :optimizations :whitespace
                                        :source-map true
                                        :pretty-print true}}
                       :test {:source-paths ["src" "test"]
                              :compiler {:output-to "resources/test/compiled.js"
                                         :optimizations :whitespace
                                         :pretty-print true}}}
              :test-commands {"unit" ["phantomjs"
                                      "resources/test/test.js"
                                      "resources/test/test.html"]}}
  :clean-targets ^{:protect false} ["out"
                                   "resources/test/compiled.js"
                                   :target-path]
  :profiles {:dev {:plugins [[lein-cljfmt "0.5.6"]]}})
