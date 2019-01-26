(defproject data-explorer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.439"]
                 [reagent "0.8.1"]]
  :plugins [[cider/cider-nrepl "0.18.0"]]
  :profiles {:dev
             {:dependencies [[org.clojure/clojurescript "1.10.439"]
                             [com.bhauman/figwheel-main "0.2.0"]
                             [cider/piggieback "0.3.8"]
                             [reagent "0.8.1"]]
              :resource-paths ["target"]
              :clean-targets ^{:protect false}["target"]
              :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}})
