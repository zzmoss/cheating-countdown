(defproject cheating-countdown "0.1.0-SNAPSHOT"
  :description "Countdown timer with a cheating mode"
  :url "http://github.com/madhuvishy/cheating-countdown"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2156"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [om "0.5.0"]
                 [com.cemerick/piggieback "0.1.3"]]

  :plugins [[lein-cljsbuild "1.0.2"]]

  :source-paths ["src"]

  :cljsbuild { 
    :builds [{:id "cheating-countdown"
              :source-paths ["src"]
              :compiler {
                :output-to "cheating_countdown.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]}
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]})
