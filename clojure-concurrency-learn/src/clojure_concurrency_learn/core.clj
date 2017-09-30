(ns clojure-concurrency-learn.core)

;; (defn foo
;;  "I don't do a whole lot."
;;  [x]
;;  (println x "Hello, World!"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; CONCURRENCY
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(instance? Runnable (fn [])) ;; All Clojure functions are instances of Runnable

;; This also means that you can use it with Thread to do asynchronous tasks
(.start (Thread. (fn [] (println "Hello World")))) ;; Here we created a function, instantiated a thread and called the start method on it

;; Like node.js you can use promises with  Clojure. Promises represent ongoing computations
(promise) ;; Note that this accepts no arguments
(deliver (promise) "Hello") ;; Delivering to a promise

;; Promises are most useful when the accept delivery from another thread
(defn myslowfn []
  (let [p (promise)] ;; create a promise
        (.start ;; call start on this thread
          (Thread. ;; create a thread
            (fn [] ;; This thread has a function that sleeps for 10 secs and then delivers a string "hello" to the promise
              (Thread/sleep 10000)
              (deliver p "Hello")
            )
          )
        )
  p) ;; Ensure that the function returns the promise so that we can access it
)
(myslowfn) ;; Call my slow function. Evaluates right away. Non-blocking
(deref (myslowfn)) ;; Extracts the value. This actually blocks till it can get the value
@ (myslowfn) ;; Shorthand for anything you want to dereference

;; Let us actually do some work while our promise is running
(let [p (myslowfn)]
  (println "Waiting for a promise ... ")
  @p
)
;; Since this is such a common pattern Clojure includes a function called Future to deal with this
;; Let us consider the following function that takes a while (simulated by a Thread/sleep) and then returns a value
(defn myslowfn []
  (Thread/sleep 5000)
  "Hello"
)

(future (myslowfn))

@ (future (myslowfn))

;; Here is a use-case for futures. Imagine if you have  a very slow logging function inside an outer function that processes very quickly. We can process this using a future as shown below

(defn slowlog [msg]
  (Thread/sleep 3000)
  (println msg)
)

(defn myfn []
  (future (slowlog "Called myfn")) ;; We can also use delay instead of future but it won't execute until dereferenced at least one
  :ok
)

(myfn)

;; Memoizing functions - Cache intermediate results

(defn fib [n]
  (if (< n 2)
    1
    (+ (fib (- n 1)) (fib (- n 2)))
  )
)

(fib 40)

(def fib (memoize fib))

(fib 90)

;; Clojure STM and Atoms
;; ref: http://clojure-doc.org/articles/language/concurrency_and_parallelism.html
;; ref: https://stackoverflow.com/questions/9132346/clojure-differences-between-ref-var-agent-atom-with-examples
;; Atoms - synchronous, uncoordinated reference type
;; What is a reference type?
;; In Clojure, values are immutable.When you attempt to modify a value (a data structure), a new value is produced instead. These are known as persistent data structures (the word "persistent" has nothing to do with storing data on disk).
;; An identity is a named entity (e.g., a list of active chat group members or a counter) that changes over time and at any given moment references a value
;; Identities in Clojure can be of several types, known as reference types.

(def count (atom 0)) ;; create an atom and assign it the value 0, give it a name using def
(reset! count 1)
(deref count) ;; Extracting values using dereferncing
@count

;; Setting values with reset is dangerous, to understand see below
(dotimes [_ 1000] ;; run loop from 0 to 999 -
  (future (reset! count (inc @count))) ;; get current value of count increment it , reset the count value to the new value and return as a future
)
(deref count) ;; this is because by the time one process completed the future resets the value

(def count (atom 0))
(dotimes [_ 1000]
  (future (swap! count inc)) ;; this ensures synchronization,  swap will also accpet function with argument
)
(deref count)

(def coount (atom 0 :validator integer?)) ;;Validates inputs

;; Atoms are not suitable for transaction type activities that need ACI

(def acc1 (atom 0 :validator #(>= % 0)))
(def acc2 (atom 0 :validator #(>= % 0)))

(reset! acc1 1000)
(reset! acc2 1000)

(defn transfer [from-acc to-acc amt]
  (swap! to-acc + amt)
  (swap! from-acc - amt)
)

(dotimes [_ 1000]
  (future (transfer acc2 acc1 100))
)

(deref acc1)
(deref acc2)

;; As we can see the amounts are not right, this is because the future fails to transmit the validation exception
;; Hence for maintaining transactional integrity we shall use Refs. Refs are for co-ordinated, synchronous updates on multiple values

(def acc1 (ref 1000 :validator #(>= % 0)))
(def acc2 (ref 1000 :validator #(>= % 0)))

(defn transfer [from-acc to-acc amt]
  (dosync ;;ensures synchronization of transactions
    (alter to-acc + amt) ;; note that we have changed the swap to alter
    (alter from-acc - amt) ;; note that you can also use commute instead of alter, if sequence of operations does not matter as it will be parallelized e.g. if you are computing averages. Commute should only be used if the operation is commutative
  )
)

(dotimes [_ 1000]
  (future (transfer acc2 acc1 100))
)

(deref acc1)
(deref acc2)

;; The exact opposite of Ref is Agent. Agents are for uncoordniated asynchronous updates on single values

(def my-agent (agent 0 :validator #(>= % 0)))

(send my-agent inc) ;; equivalent to alter for refs or swap for atoms

(deref my-agent) ;; Note that the value does not display what we expect it to. This is because the future may not have had sufficient time to act upon the value

(def new-value 0)
(restart-agent my-agent new-value) ;; This can be used if the agent is stopped (most likely due to failing validataions), it then has to be re-initialized
(def my-agent (agent 0 :validator #(>= % 0) :error-mode :continue :error-handler println)) ;; This is another way of creating an agent that recovers from failure. Error handler will take function that will manage error
