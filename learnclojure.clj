(ns learnclojure)
;; This is the first thing you do when you create a Clojure program

(+ 2 2)
;; Note the RPN

(print "Hello World")
;; Note that print returns nil. The side effects are written to console

(* 3 (- 2 1))
;; Move from the inside out, note the homoiconicity

(type 1)
;; Clojure uses Java types, by default it uses Long and Double
(type 1.1)
(type true)
(type "Hello")

;; There are two interesting types that are Clojure specific
;; Keyword
(type :a)
(type (keyword "a"))
;; Symbol
(type (quote a))
(type 'a)
;; For further details see https://clojure.org/reference/data_structures#Keywords
;; and https://www.cis.upenn.edu/~matuszek/Concise%20Guides/Concise%20Clojure.html


;; Now let us look at some collections
;; Lists - are linked lists are efficient if we are accessing the head of list
(type '(1 2 3))
(type (list 1 3 2)) ;; this uses the list constructor
(first (list 1 3 4))

;; Vectors
(type (vector 1 3 4)) ;; this uses the vector constructor
(type [1 3 4.0]) ;; Another way of representing vector
;; Other representations include
(type (vector 1 2 3))
;; More efficient for position based array style access
(nth (vector 1 5 7) 2)

;; Maps - used for key value pairs
{:a 1 :b 2 :c 4}
(type {:a 1 :b 2}) ;; This is an Array Map
(type (hash-map :a 1 :b 2)) ;; ArrayMaps preserve order but are slow, above a certain size it makes sense to use hash maps


;; Sets
(type #{ 1 2 4})
(type (hash-set 1 3 4))

;; Variables can be initialized using two methods (Note that these are not variables in the traditional sense of the word, the values are immutable)
;; Method 1: using def. This defines the name to have the value of the expression in current namespace
(def x "Hello Clojure \n")
(print x)

;; Using let - defines local names and bind values to them
(let
  [x "steve"]
  (print "Hello, " x)
)

(print x)


;; Basic flow control

(if (empty? x) ;; this is the predicate. Note that if is a macro not a function
  "X is empty" ;; if true
  "X is not empty" ;; if false
)

;; If you want to do more than one thing use, you can use the do notation like in Haskell (this can have side-effects)
;; Use only for things like logging
(if (empty? x)
  nil
  (do
    (println "OK")
    :ok
  )
)

;; Other variants of this include if-not

(if-not (empty? x) ;; inverse of previous predicate due to if-not
  (
    do
      (println "Second OK")
      :ok
  )
)

;; This logic can be implemented usign when and when-not
(when-not (empty? x)
  (do
    (println "Third OK")
    :ok
  )
)

;; switch case
(case x
  "Hello" :hello
  "Goodbye" :goodbye
  :nothing
)

;; Another way to pattern match is to use cond (classic LISP style)
(cond
  (= x "Goodbye") :goodbye
  (= (reverse x) "olleh") :hello
  :otherwise :nothing
) ;; note that otherwise is always true, so it is always true

;; Now we move on to functions
;; Note that the argument list to a function is a vector
;; In Clojure you use lists only if you want to preserve sequence somehow
(fn [] "Hello World")

((fn [] "Hello World")) ;; Note that now the function will be evaluated as it is just an element in a list

(def hello (fn [] "Hello function name")) ;; Give the function a name
(hello) ;; evaluate function and convert to first element of list

(defn hello2 [] "Hello") ;; Note like Ruby there is no need for an explicit return, all expressions are evaluated and the last one is returned
(hello2)

(defn hello [name] (str "Hello, "  name)) ;; This is how you create a function with arguments
(hello "Jane")
(hello (vector "Jane"))

(require '[clojure.repl :refer [doc]]) ;; Used for docstring to create documentation
(defn hello4 "Greets a person given name and title" [name, title] (str "Hello " title " " name)) ;; This shows us how to create documentation and pass multiple arguments
(doc hello4)
(hello4 "Anderson" "Mr.") ;; Note we did not have to create a vector here

;; Let us say you want to pass variable number of arguments like (*args, **kwargs) in Python. This is how you do it
(defn hello [& args]
  (str "Hello" (apply str args))
)
(hello "Fred" "Jack")
(hello "Peter" "Paul" "Mary")


;; Function polymorphism (overloading)
(defn hello5
  ([] "Hello World!")
  ([name] (str "Hello" name))
)
(hello5)
(hello5 "Peter")


;; Passing hash maps
(defn hello [config] ;; variants include (defn  hello [name :name ... and (defn hello [name :name :as config ... in both cases replace ':name config' below with 'name'
  (str "Hello " (:name config))
)
(def person {:name "Jane" :occupation "Tailor" :country "US"})
(hello person)

;; More on collectiona
;; All Clojure collections are immutable and persistent
(def x (list 1 3 4))
(cons 0 x)
(conj x 0)
(print x)
(first x)
(last x)
(nth x 1)
