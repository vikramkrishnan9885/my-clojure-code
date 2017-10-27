(ns clojure-design-patterns.core)

;; PATTERN 1: FUNCTOR PATTERN (FUNCTIONAL INTERFACE)
;; FROM FUNCTIONAL PROGRAMMING PATTERNS (HENCEFORTH REFERRED TO AS FPP) BOOK
;; Try to solve the expression problem by creating interfaces with single
;; functions

;; PROBLEM STATEMENT: SORT A COLLECTION DIFFERENTLY FROM NATURAL ORDERING
;; Sort a Person by First Name

(def p1 {:first-name "Michael" :last-name "Fletcher"})
(def p2 {:first-name "John" :last-name "Smith"})
(def p3 {:first-name "Robert" :last-name "Jones"})
(def people [p1 p2 p3])

(sort (fn [p1 p2] (compare (p1 :first-name) (p2 :first-name))) people)

;; => ({:first-name "John", :last-name "Smith"} {:first-name "Michael", :last-name "Fletcher"} {:first-name "Robert", :last-name "Jones"})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; PATTERN 2: CLOSURE (FROM FPP BOOK)
;; A closure wraps up a function along with the state availble to it when created

;; Having closures and higher-order functions can simplify many common 
;; patterns (Command, Template Method, and Strategy to name a few) to such 
;; an extent that they almost disappear.

;; PROBLEM STATEMENT: MAKE COMPOSED COMPARISON

(def p1 {:first-name "John" :middle-name "" :last-name "Adams" })
(def p2 {:first-name "John" :middle-name "Quincy" :last-name "Adams"})

(defn first-name-comparison [p1 p2]
  (compare (:first-name p1)(:first-name p2))
)
(defn last-name-comparison [p1 p2]
  (compare (:last-name p1)(:first-name p2))
)
(def first-and-last-name-comparison
  (make-composed-comparison
    first-name-comparison last-name-comparison
  )
)
(defn make-composed-comparison [& comparisons]
  (fn [p1 p2]
    (let 
      [results (for [comparison comparisons] (comparison p1 p2))
        first-non-zero-result
        (some 
          (fn [result]
            (if (not (= 0 result))
              result
              nil
            )
          ) 
          results
        )
      ]
      (if (nil? first-non-zero-result)
        0
        first-non-zero-result
      )
    )
  )
)
(first-and-last-name-comparison p1 p2)

;; PATTERN 3: COMMAND 
;; FROM FPP BOOK
;; Turn method invocation into an object and execute it in a central location

;; PROBLEM STATEMENT: BUILD A CASH REGISTER THAT:
;; 1. HANDLES ONLY DOLLARS
;; 2. CONTAINS THE TOTAL AMOUNT OF CASH
;; 3. CASH CAN ONLY BE ADDED TO THE REGISTER
;; 4. KEEP LOG OF TRANSACTIONS

(defn make-cash-register []
  (let [register (atom 0)]
    (set-validator! register (fn [new-total] (>= new-total 0))
    )
    register
  )
)

(defn add-cash [register to-add]
  (swap! register + to-add)
)

(defn reset [register]
  (swap! register (fn [oldval] 0))
)

(defn make-purchase [register amount]
  (fn []
    (println (str "Purchase in amount " amount))
    (add-cash register amount)
  )
)

;; let us use it
(def register (make-cash-register))
(add-cash register 100)
(def purchase-1 (make-purchase register 50))
(purchase-1)

(def purchases (atom []))
(defn execute-purchase [purchase]
  (swap! purchases conj purchase)
  (purchase)
)
(execute-purchase purchase-1)

;; PATTERN 4: BUILDER PATTERN
;; FROM FPP BOOK
;; A simple way to create new objects based off existing ones, setting some 
;; attributes to new values