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

;; Other variants of this include
