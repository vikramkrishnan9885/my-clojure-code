(defproject project1 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data/json "0.2.3"]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [ring "1.2.0"]]
  :plugins [[lein-ring "0.8.7"]] ;; Lein-Ring is a Leiningen plugin that automates common Ring tasks.  It provides commands to start a development web server, and to turn a Ring handler into a standard war file.
  ;; For basic web applications use an uberjar file. For more advanced use cases, go for a war file
  :ring {:handler project1.core/example-handler ;; unlike java servlet web apps only one handler per project
    :init project1.core/on-init ;; Can be used to for config init
    :destroy project1.core/on-destroy ;; closing connections etc
    :port 40001
  }
  )
