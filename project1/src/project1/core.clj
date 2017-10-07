(ns project1.core)

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defn example-handler [request]
  {:body "Hello"})

(defn on-init []
  (println "Initializing web app ... "))

(defn on-destroy []
  (println "Destroying web app ... ")) ;; May not be invoked if there is a power loss etc
