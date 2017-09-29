(defproject learnclojure "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :main learnclojure.core ;; This specifies the location of the main function. Without this, we have to run 'lein run -m learnclojure.core'
  :aot [learnclojure.core] ;; When we use gen-class to create a Java class, it won't be visible in the REPL till it is compiled. :aot tells clojure that specific classes or namespaces in our case core need to be compiled ahead-of-time (:aot)
)
