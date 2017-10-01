(defproject learn-ring "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring "1.6.2"]] ;; Ring is to Clojure what Rack is to Ruby or WSGI to Python
  :main ^:skip-aot learn-ring.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
