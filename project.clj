(defproject tcsavage/clj-boolean "0.1.0-SNAPSHOT"
  :description "Boolean algebra"
  :url "https://github.com/tcsavage/clj-boolean"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/math.combinatorics "0.1.4"]
                 [org.clojure/tools.logging "0.4.0"]]
  :plugins [[lein-codox "0.10.5"]]
  :profiles {:dev {:dependencies [[ch.qos.logback/logback-classic "1.2.3"]]
                   :source-paths ["dev"]
                   :resource-paths ["dev-resources"]}})
