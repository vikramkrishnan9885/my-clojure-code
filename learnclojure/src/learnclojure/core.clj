(ns learnclojure.core) ;; Look at this name space

;; (require 'clojure.string) ;; This is similar to import in Python or Java. Note that the module name needs to be quoted so that it is not evaluated
;; (require '[clojure.string :as s]) ;; So that you don't have to keep typing clojure.string. Also, note the vector
(require '[clojure.string :refer [split]]) ;; Ensure that when you do this, you don't import same function from different namespaces
;; (require '[clojure.string :refer :all]) ;; This is dangerous. Don't do this or (use 'clojure.string)
(import 'java.util.Date) ;; Importing java functions
;; (import '[java.util Date Calendar]) ;; If you want to import multiple classes from same Java module

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

;; (clojure.string/split "a,b,c" #",")
;; (s/split "a,b,c" #",")
(split "a,b,c" #",")

(Date.) ;; dot after Date denotes constructor

;; To create an executable we need a -main function similar to Java main
(defn -main []
  (foo "Main")
)
