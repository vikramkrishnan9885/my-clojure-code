(ns learn-safety)

(deftype MyPerson [name])

(MyPerson. "Cornelius")
(->MyPerson "Timothy") ;; Do not, repeat do not insert a space between -> and MyPerson
(.name (->MyPerson "Coriolanus")) ;; Note we cannot set values, only construct objects - immutable

;; defrecord is a full implementation of clojure hash-map

(defrecord MyPersonRecord  [name])
(:name (->MyPersonRecord "Jorge"))
;; alternately you can create a new record using map
(map->MyPersonRecord {:name "Jorge"})

;; both deftype and defrecord can be used to implement Java interfaces
(deftype MyRunnable [name]
  Runnable ;; call interface
    (run [this] (println (.name this)))) ;; supply implementation of methods

(.run (->MyRunnable "Francis"))

;; MULTIMETHODS

;; Rationale behind multimethods:
;; All programming languages have faced a problem called the expression problem, which is the question of how to introduce new data types and new methods acting on those data types
;; In OOP languages adding a new data type is easy: just create a class
;; However, adding a new functionality involves editing each and every class
;; In FP, the problem is reversed, you can easily create new methods, but then you need to edit ensure that it can accept all the relevant data types

(defmulti hello  :language) ;; remember key words act as functions on maps and return value at key

(defmethod hello ::french [_] "Bonjour");; :: is a namespace keyword
(defmethod hello :default [_] "Hello") ;; specify a default
(defmethod hello ::spanish [_] "Hola!")
(defmethod hello ::english [_] "Howdy")

(hello {:language ::english}) ;; Wont work until we define a default
(hello {:language ::spanish})


;; you can use derive to establish relationships between keys : parent and child
(derive ::cockney ::english)
(hello {:language ::cockney})

(isa? ::cockney ::english)

;; Look at this https://adambard.com/blog/structured-clojure-protocols-and-multimethods/
;; Open vs closed - are you ok with code that specifies a concrete set of choices and requires modification to add new cases? Or do you want an open system that can be extended without modifying the existing code. Multimethods and protocols are open, case and cond are closed.
;;Type vs value - do you want to dispatch based on type of a single argument or based on values or types of multiple arguments? Are the values you are choosing between constants or expressions that require evaluation?
;;General guidelines:
;;
;;open extension and type-based dispatch => protocols
;;open extension and value-based dispatch => multimethods
;;closed constant choices => case
;;closed expressions => cond

;; PROTOCOLS

;; A lot like Java interfaces. Can be attached to classes, methods and types at run time
(defprotocol Shape ;; defprotocol name
  (area [this]) ;; method names with method signature: list of arguments
  (perimeter [this])
)

(defrecord Circle [r] ;; This is r
  Shape ;; protocol
  (area [this] (* (Math/PI) (:r this) (:r this))) ;; This is :r
  (perimeter [this] (* 2 (:r this) (Math/PI)))
);; For some reason .Math PI doesn't work Math/PI

(area (->Circle 5))
(perimeter (->Circle 5))

(deftype Square [s])

(extend-protocol Shape
  Square
  (area [this] (* (.s this) (.s this)))
  (perimeter [this] (* 4 (.s this)))
)

(area (->Square 10))
(perimeter (->Square 10))

(import '[java.util Date Calendar])
(defprotocol RoundableDate
  (nearest-day [this]))
(extend-protocol RoundableDate
  ;;
  Long
  (nearest-day [this] (- this (mod this 86400000)))
  ;;
  Date
  (nearest-day [this] (Date. (nearest-day (.getTime this))))
  ;;
  Calendar
  (nearest-day [this]
    (doto this
      (.set Calendar/HOUR 0)
      (.set Calendar/MINUTE 0)
      (.set Calendar/SECOND 0)
      (.set Calendar/MILLISECOND 0)
    )
  )
)
(nearest-day (System/currentTimeMillis))
(nearest-day (Date.))
(nearest-day (Calendar/getInstance))