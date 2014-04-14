(defproject cheating-countdown "0.1.0-SNAPSHOT"
  :description "Countdown timer with a cheating mode"
  :url "http://github.com/madhuvishy/cheating-countdown"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2173"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [om "0.5.3"]
                 [com.cemerick/piggieback "0.1.3"]]

  :plugins [[lein-cljsbuild "1.0.2"]]

  :source-paths ["src"]

  :cljsbuild { 
    :builds [{:id "dev"
              :source-paths ["src"]
              :compiler {
                :output-to "cheating_countdown_dev.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}
             {:id "release"
              :source-paths ["src"]
              :compiler {
                :output-to "cheating_countdown_release.js"
                :optimizations :advanced 
                :pretty-print false
                :preamble ["react/react.min.js"]
                :externs ["react/externs/react.js"
                          "resources/js/jquery.min.js"
                          "resources/js/jquery.datetimepicker.js" ]}}]}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]})
