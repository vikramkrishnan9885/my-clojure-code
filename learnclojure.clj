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
;; (doc first) ;; Didn't work

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
(cons 0 x) ;; Cons will convert to list
(conj x 0)
(print x)
(first x)
(last x)
(nth x 1)

(def v (vector 1 3 4))
(conj v 0);; Note that this differs from conj for a list with only added to head, this adds to the end of the vector

(type (concat x v)) ;; Note concat returns a lazy seq

(assoc {:a 1} :b 2) ;; Assoc adds key to a given key-value map
(assoc-in {:settings {:a 1 :b 2}} [:settings :a] "a")
(update-in {:settings {:a 1 :b 2}} [:settings :a] inc) ;; Instead of taking a new value, it takes a function that updates an existing value


(def m {:a 1 :b 2})
(get m :a)
(:a m)
(m :a)
(:c m)
(m :c)

;; Let us play around with sets for a while
(def s #{1 2 3})
(conj s 4) ;; Adds to set
(conj s 3) ;; Has no effect, sets store only distinct values
(disj s 3) ;; Dis-join is the reverse of con-join
(contains? s 3) ;; Note that the original set is not modified and we get a copy in return
(get s 4) ;; As above

;; Recursion

(defn my-sum [total vals] ;; arguments are accumulator and the incoming values
  (if (empty? vals) ;; Write your terminal condition
    total
    (my-sum (+ (first vals) total) (rest vals)) ;; call the same function back again
  )
)

(my-sum 0 [0 1 2 3 4]) ;; This function's API you will notice is not ideal, people have to pass an accumulator let us improve upon this

;; We use function overloading explained above
(defn my-sum
  ([vals] (my-sum 0 vals))
  ([total vals]
    (if (empty? vals) ;; Write your terminal condition
      total
      (my-sum (+ (first vals) total) (rest vals)) ;; call the same function back again
    )
  )
)

(my-sum [0 1 4 7]) ;; This function is pretty sweet, however it suffers from a major weakness, there is no TCO

(defn my-sum
  ([vals] (my-sum 0 vals))
  ([total vals]
    (if (empty? vals) ;; Write your terminal condition
      total
      (recur (+ (first vals) total) (rest vals)) ;; Note we have replace function call with recur
    )
  )
)
;; By replacing the second my-sum with recur we now have TCO
(my-sum [0 1 4 7])

;; BTW you can also use loops like in any other programming language
(defn my-sum [vals]
  (loop [total 0 values vals] ;; Initialize variables
    (if (empty? values) ;; The until condition
      total
      (recur (+ (first values) total) (rest values))
    )
  )
)
(my-sum [0 1 4 7])

;; We will now use reduce function to calculate the sum
;; syntax is reduce(function initial_value list)
(def x (list 0 1 2 3 4))
(def y 0)
(defn sum [total vals] (+ total vals))
(reduce sum y x)
(+ (+ (+ (+ (+ 0 0) 1) 2) 3) 4) ;; This is what reduce does underneath the hood
(reduce + [1 3 4]) ;; If no initialization values are provided then it adds the first two values

(defn filter-even [acc next-val]
  (if (even? next-val)
    (conj acc next-val)
    acc
  )
)
(reduce filter-even [] [0 1 2 3 4 5 6]) ;; reduce applies function to list
(filter even? [0 1 2 4 7 9]) ;; In real life just use filter

(map inc [0 1 2 3 4 5 6]) ;; when you wish to apply the same function to all values in a list map is a perfectly good option
;; This is a useful link https://www.braveclojure.com/core-functions-in-depth/

(defn group-even [acc next-val]
  (let [key (if (even? next-val) :even :odd)]
    (update-in acc [key] #(conj % next-val)) ;; Anonymous functions please refer to https://clojure.org/api/cheatsheet
  )
)

(reduce group-even {} [0 1 2 4 7 8])

(group-by #(if (even? %) :even :odd) [0 1 2 4 7 8])


