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
;; Turn method invocation into an objhttps://www.facebook.com/ibrahim.manna?hc_ref=ARQSMkaCHomtI31OxJ57lid_cMYSyNcVhnvVy1CFsvG91ah3t2zAeIdyHZ_3qXL-ejAect and execute it in a central location

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
;; Use plain old immutable maps to model aggregate data.

(def p
  {
    :first-name "John"
    :middle-name "Quincy"
    :last-name "Adams"
  }
)
(into {} (for [[k, v] p] [k (.toUpperCase v)]))

;; The second way to model data in Clojure is to use a record. A record exposes
;; a map-like interface; so you can still use the full power of Clojure’s sequence
;; library on it, but records have a few advantages over maps.
;; First, records are generally more performant. In addition, records define a
;; type that can participate in Clojure’s polymorphism. To use the old objectoriented chestnut, it allows us to define a make-noise that will bark when passed
;; a dog and meow when passed a cat. In addition, records let us constrain the
;; attributes that we can put into a data structure.
;; Generally, a good way to work in Clojure is to start off modeling your data
;; using maps and then switch to records when you need the additional speed,
;; you need to use polymorphism, or you just want to constrain the names of
;; the attributes you’re handling.
(defrecord Cat [color name])
(defrecord Dog [color name])
(defprotocol NoiseMaker
  (make-noise [this])
)
(defrecord NoisyCat [color name]
  NoiseMaker
  (make-noise [this] (str (:name this) "meows!"))
)
(defrecord NoisyDog [color name]
  NoiseMaker
  (make-noise [this] (str (:name this) "barks!"))
)
(def noisy-cat (NoisyCat. "Calico" "Fuzzy McBootings"))
(def noisy-dog (NoisyDog. "Brown" "Brown Dog"))
(make-noise noisy-cat)
(make-noise noisy-dog)

;; PATTERN 5: ITERATOR
;; We can replace an iterator with a combination of higher-order functions and sequence 
;; comprehensions. A sequence comprehension is a clever technique that lets us take one 
;; sequence and transform it into another in some sophisticated ways. 

;; Many basic uses of Iterator can be replaced by simple higher-order functions.
;; For instance, summing a sequence can be done in Clojure using the reduce higher-order function.
;; Sequence comprehensions provide a concise way to create a new sequence from an old one, including the ability to filter out unwanted values.
;; Another useful HoF is map

(def vowel? #{\a \e \i \o \u})
(defn vowels-in-word [word]
  (set (filter vowel? word)))
(vowels-in-word "onomotopeia")
;; => #{\a \e \i \o}
(vowels-in-word "yak")
;; => #{\a}

;; We want little hello prepender, prepend-hello. For example, we 
;; simply use map to map a function that prepends "Hello, " to each
;; name in a sequence of names.
(defn add-hello [name]
  (str "Hello " name))
(defn prepend-hello [names]
  (map add-hello names))
(prepend-hello ["Mike" "John" "Tim"])
;; => ("Hello Mike" "Hello John" "Hello Tim")

;; Example with reduce
(defn sum-sequence[s]
  {:pre [(not (empty? s))]}
  (reduce + s)
)
(sum-sequence [1 2 3 4 5])
;; => 15

;; Sequence Comprehensions
;; Clojure does this using the for comprehension
;; close-zip? uses Clojure's set-as-function feature
(def close-zip? #{19123 19103})
(defn generate-greetings [people]
  (for [{:keys [name address]}
    people
    :when (close-zip? (address :zip-code))
  ]
  (str "Hello " name " and welcome to Lambda Grill")
  )
)

;; PATTERN 6: REPLACING TEMPLATE METHOD
;; Our functional replacement for Template Method will satisfy its intent, which
;; is to create a skeleton for some algorithm and let callers plug in the details.
;; Instead of using classes to implement our suboperations, we’ll use higherorder functions; and instead of relying on subclassing, we’ll rely on function
;; composition.

;; we’ll use Pattern 16, Function Builder, on page 167, named
;; make-grade-reporter, to compose together a function that converts numeric grades
;; to letter grades and a function that prints a report. The make-grade-reporter
;; returns a function that maps num-to-letter over a sequence of numeric grades.

(defn make-grade-reporter [num-to-letter print-grade-report]
  (fn [grades]
    (print-grade-report (map num-to-letter grades))))

(defn full-grade-converter [grade]
  (cond
    (and (<= grade 5.0) (> grade 4.0)) "A"
    (and (<= grade 4.0) (> grade 3.0)) "B"
    (and (<= grade 3.0) (> grade 2.0)) "C"
    (and (<= grade 2.0) (> grade 0)) "D"
    (= grade 0) "F"
    :else "N/A"))

(defn print-histogram [grades]
  (let [grouped (group-by identity grades)
    counts (sort (map
                    (fn [[grade grades]] [grade (count grades)])
                    grouped))]
  (doseq [[grade num] counts]
    (println (str grade ":" (apply str (repeat num "*")))))))

(def full-grade-reporter (make-grade-reporter full-grade-converter print-histogram))
(def sample-grades [5.0 4.0 4.4 2.2 3.3 3.5])
(full-grade-reporter sample-grades)

(defn print-all-grades [grades]
  (doseq [grade grades]
    (println "Grade is:" grade)))

(def full-grade-reporter
(make-grade-reporter full-grade-converter  print-all-grades))

(full-grade-reporter sample-grades)

;; PATTERN 7 : REPLACING STRATEGY
;; To define an algorithm in abstract terms so it can be implemented in several
;; different ways, and to allow it to be injected into clients so it can be used
;; across several different clients
;; Strategy has a few parts. The first is an interface that represents some algorithm, such as a bit of validation logic or a sorting routine. The second is one
;; or more implementations of that interface; these are the strategy classes
;; themselves. Finally, one or more clients use the strategy objects.
(defn first-name-valid? [person]
  (not (nil? (:first-name person))))

(defn full-name-valid? [person]
  (and
    (not (nil? (:first-name person)))
    (not (nil? (:middle-name person)))
    (not (nil? (:last-name person)))))

(defn person-collector [valid?]
  (let [valid-people (atom [])]
    (fn [person]
      (if (valid? person)
        (swap! valid-people conj person))
      @valid-people)))


(def p1 {:first-name "john" :middle-name "quincy" :last-name "adams"})
(def p2 {:first-name "mike" :middle-name nil :last-name "adams"})
(def p3 {:first-name nil :middle-name nil :last-name nil})

(def first-name-valid-collector (person-collector first-name-valid?))
(def full-name-valid-collector (person-collector full-name-valid?))

(first-name-valid-collector p1)
(first-name-valid-collector p3)
(full-name-valid-collector p1)
(full-name-valid-collector p3)

;; PATTERN 8 REPLACNG NULL OBJECT
;; To avoid scattering null checks throughout our code by encapsulating the
;; action taken for null references into a surrogate null object
;; Common style leads to scattering null handling logic throughout our code, often
;; repeating it. If we forget to check for null it may lead to a program crashing
;; NullPointerException, even if there is a reasonable default behavior that can handle
;; the lack of a value.

;; Clojure doesn't really help here

;; PATTERN 9 DECORATOR
;; To add behavior to an individual object rather than to an entire class of
;; objects—this allows us to change the behavior of an existing class.
;; The essence of Decorator is wrapping an existing class with a new one so that
;; the new class can tweak the behavior of the existing one. In the functional
;; world, one simple replacement is to create a higher-order function that takes
;; in the existing function and returns a new, wrapped function.

(defn add [a b] (+ a b))
(defn subtract [a b] (- a b))
(defn multiply [a b] (* a b))
(defn divide [a b] (/ a b))

(defn make-logger [calc-fn]
  (fn [a b]
    (let [result (calc-fn a b)]
      (println (str "Result is: " result))
      result)))

(def logging-add (make-logger add))
(def logging-subtract (make-logger subtract))
(def logging-multiply (make-logger multiply))
(def logging-divide (make-logger divide))

(logging-add 2 3)

;; PATTERN 10: VISITOR PATTERN