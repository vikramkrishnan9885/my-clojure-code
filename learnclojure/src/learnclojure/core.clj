(ns learnclojure.core) ;; Look at this name space

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

;; To create an executable we need a -main function similar to Java main
(defn -main []
  (foo "Main")
)
