(ns testing-functional-design-patterns.core)

;; PATTERN 12 - TAIL RECURSION

;; Let’s take a look at a recursive solution to a simple problem. We’ve got a
;; sequence of first names and a sequence of last names, and we want to put
;; them together to make people. To solve this, we need to go through both
;; sequences in lock step.

;; Scala solves this problem using the @tailrec annotation

;; Tail Recursion - Clojure does not have TCO (Boo!)
;; Instead of providing TCO, Clojure gives us two forms loop and recur

(defn make-people [first-names last-names]
  (loop [first-names first-names last-names last-names people []]
    (if [seq first-names]
      (recur
        (rest first-names)
        (rest last-names)
        (conj people {:first (first-names) :last (first last-names)})
        people
      )
    )
  )
)

;; The code snippet first-names first-names last-names last-names people [] might look a
;; little funny, but all it’s doing is initializing the first-names and last-names that
;; we’re defining in the loop to be the values that were passed into the function
;; and the people to an empty vector.
;; The bulk of the example is in the if expression. If the sequence of first names
;; still has items in it, then we take the first item from each sequence, create a
;; map to represent the person, and conj it onto our people accumulator.
;; Once we’ve conjed the new person onto the people vector, we use recur to jump
;; back to the recursion point we defined with loop.

;; Since tail recursion is equivalent to iteration, it’s really a fairly low-level
;; operation. There’s generally some higher-level, more-declarative way to solve
;; a problem than using tail recursion. For instance, here’s a shorter version of
;; the solution to our person-making example that takes advantage of some
;; higher-order functions in Clojure:


(defn shorter-make-people [first-names last-names]
(for [[first last] (partition 2 (interleave first-names last-names))]
{:first first :last last}))

;; https://en.wikibooks.org/wiki/Clojure_Programming/By_Example - this has  a nice example on loop recur
;; https://clojurebridge.github.io/community-docs/docs/clojure/loop/
;; https://clojurebridge.github.io/community-docs/docs/clojure/recur/
;; the above two show how to initialize loop variable and use recur where you would normally have a functional
;; call in a language like Haskell
;; https://clojuredocs.org/clojure.core/loop
;; https://clojuredocs.org/clojure.core/recur
;; https://forum.freecodecamp.org/t/clojure-loop-recur/18418
;; https://stackoverflow.com/questions/33819358/what-is-the-difference-between-loop-recur-and-recur-by-itself
;; https://programming-pages.com/2012/01/23/loops-in-clojure/


;; PATTERN 13: MUTUAL RECURSION
;; To use mutually recursive functions to express certain algorithms, such as
;; walking tree-like data structures, recursive descent parsing, and state machine
;; manipulations

;; For instance, finite state machines are a great way of modeling many classes
;; of problems, and mutual recursion is a great way to program them. Network
;; protocols, many physical systems like vending machines and elevators, and
;; parsing semistructured text can all be done with state machines.

;; In this pattern, we’ll look at some problems that can be solved cleanly using
;; mutual recursion. Since the JVM doesn’t support tail recursive optimization,
;; Scala and Clojure have to use a neat trick to support practical mutual
;; recursion, just as they did with normal tail recursion, to avoid running out
;; of stack space.

;; For mutual recursion, this trick is called a trampoline. Instead of making
;; mutually recursive calls directly, we return a function that would make the
;; desired call, and we let the compiler or runtime take care of the rest.

;; In this example, we’ll use Mutual Recursion to build a simple state machine
;; that takes a sequence of transitions between the different phases of matter—liquid,
;; solid, vapor, and plasma—and verifies that the sequence is valid. For
;; instance, it’s possible to go from solid to liquid, but not from solid to plasma.
;; Each state in the machine is represented by a function, and the transitions
;; are represented by a sequence of transition names, like condensation and vaporization.

;; A state function picks the first transition off of the sequence and, if it’s
;; valid, calls the function that gets it to where it should transition, passing it
;; the remainder of the transitions. If the transition isn’t valid, we stop and
;; return false.

;; For example, if we’re in the solid state and the transition we see is melting, then
;; we call the liquid function. If it’s condensation, which isn’t a valid transition out
;; of the solid state, then we immediately return false.

(declare plasma vapor liquid solid)
;; https://clojuredocs.org/clojure.core/declare

(defn plasma [[transition & rest-transitions]]
  #(case transition
     nil true
     :deionization (vapor rest-transitions)
     :false))
;; https://cljs.github.io/api/syntax/function
;; https://en.wikibooks.org/wiki/Clojure_Programming/Examples/API_Examples/Function_Tools##()
;; https://clojuredocs.org/clojure.core/fn

(defn vapor [[transition & rest-transitions]]
  #(case transition
     nil true
     :condensation (liquid rest-transitions)
     :deposition (solid rest-transitions)
     :ionization (plasma rest-transitions)
     false))

(defn liquid [[transition & rest-transitions]]
  #(case transition
     nil true
     :vaporization (vapor rest-transitions)
     :freezing (solid rest-transitions)
     false))

(defn solid [[transition & rest-transitions]]
  #(case transition
     nil true
     :melting (liquid rest-transitions)
     :sublimation (vapor rest-transitions)
     false))

(def valid-sequence [:melting :vaporization :ionization :deionization])
(def invalid-sequence [:vaporization :freezing])

;; PATTERN 14: Filter-Map-Reduce
;; SUPER IMP: https://www.braveclojure.com/quests/reducers/know-your-reducers/


;; HOW TO DO JOINS IN CLOJURE - JOINING TWO MAPS
;; https://clojuredocs.org/clojure.core/merge
;; https://clojuredocs.org/clojure.core/merge-with
;; https://clojuredocs.org/clojure.set/join
;; https://stackoverflow.com/questions/27894801/is-there-a-clojure-function-to-join-two-list-of-maps
;; http://www.markhneedham.com/blog/2013/09/17/clojure-merge-two-maps-but-only-keep-the-keys-of-one-of-them/
;; http://www.markhneedham.com/blog/2013/09/17/clojure-updating-keys-in-a-map/
;; https://github.com/clojure-cookbook/clojure-cookbook/blob/master/02_composite-data/2-23_combining-maps.asciidoc
;; https://en.wikibooks.org/wiki/Clojure_Programming/Examples/API_Examples/Hash-map_tools

(defn calculate-discount [prices]
(reduce +
(map (fn [price] (* price 0.10))
(filter (fn [price] (>= price 20.0)) prices))))

;; PATTERN 15: CHAIN OF OPERATIONS

;; To chain a sequence of computations together—this allows us to work
;; cleanly with immutable data without storing lots of temporary results.
;; Useful for Builder-style stuff


;; EXAMPLE 1: FUNCTION CALL CHAINING
(def v1
{:title "Pianocat Plays Carnegie Hall"
:type :cat
:length 300})
(def v2
{:title "Paint Drying"
:type :home-improvement
:length 600})
(def v3
{:title "Fuzzy McMittens Live At The Apollo"
:type :cat
:length 200})
(def videos [v1 v2 v3])

;; Method 1:
(defn cat-time [videos]
  (apply +
    (map :length
      (filter (fn [video] (= :cat (:type video))) videos))))

(cat-time videos)

;; Method 2: Chaining macros : Much better
(defn more-cat-time [videos]
  (->> videos
    (filter (fn [video] (= :cat (:type video))))
    (map :length)
    (apply +)))
 (more-cat-time videos)

;; EXAMPLE 2: CHAINING USING SEQUENCE COMPREHENSIONS

;; A common use for Chain of Operations is that we need to perform multiple
;; operations on values inside of some container type. This is especially common
;; in statically typed languages like Scala.

(def v1 [42])
(def v2 [8])

(for [i1 v1 i2 v2] (+ i1 i2))

;; For instance, we may have a series of Option values that we want to combine
;; into a single value, returning None if any of them are None. There are several
;; ways to do so, but the most concise relies on using a for comprehension to
;; pick out the values and yield a result.

;; If one of our vectors is the empty vector, then for will result in an empty
;; sequence.

(def v3 [])


(for [i1 v1 i3 v3] (+ i1 i3)) ;;()

;; Even though Clojure’s sequence comprehension works much the same as
;; Scala’s, the lack of static typing and the Option type means that the sort of
;; chaining we saw in Scala isn’t idiomatic. Instead we generally rely on chaining
;; together functions with explicit null checks.

;; The examples we saw in Chaining Using Sequence Comprehensions
;; are examples of the sequence or list monad.
;;  They make it natural to chain together operations on a container type while
;; operating on the data inside of the container.
;; In the programming world, monads are most commonly known as a way to
;; get IO and other nonpure features into a purely functional language. From
;; the examples we saw above, it may not be immediately apparent what monads
;; have to do with IO in a purely functional language.
;; Since neither Scala nor Clojure make use of monads in this way, we won’t go
;; into it in detail here. The general reason, however, is that the monadic container
;; type can carry along some extra information through the call chain.
;; For instance, a monad to do IO would gather up all of the IO done through
;; the Chain of Operations and then hand it off to a runtime when done. The
;; runtime would then be responsible for performing the IO.

;; PATTERN 16: FUNCTION BUILDER
;; Create a function that itself creates functions. allowing us to synthesize behaviors on the fly

;; To use Function Builder, we write a higher-order function that returns a
;; function. The Function Builder implementation encodes some pattern we’ve discovered.

;; Example 1: Discount Calculator Builder

(defn discount [percentage]
{:pre [(and (>= percentage 0) (<= percentage 100))]}
(fn [price] (- price (* price percentage 0.01))))

(defn disc50 [price] ((discount 50) price))

(disc50 200)

;; Example 2: Marp Key Selector
;; Create a function that extracts values out of maps nested within each other
;;  Clojure has a handy function called get-in, which is tailor-made to pick values out of deeply nested maps.


(defn selector [& path]
{:pre [(not (empty? path))]}
(fn [ds] (get-in ds path)))

(def person {:address {:street {:name "Fake St."}}})
(def streetName (selector :address :street :name))
(streetName person)

;; Example 3: Function Composition (Super Important)
;; Chain function invocations together
;; To quote from the book:  One common situation that
;; comes up in web application frameworks is the need to pass an HTTP request
;; through a series of user-defined chunks of code. J2EE’s servlet filters,2 which
;; pass a request through a chain of filters before it is handled, are a common
;; example of such a filter chain.
;; Filter chains allow application code to do anything that needs to be done before
;; request handling, like decrypting and decompressing the request, checking
;; authentication credentials, logging to a request log, and so forth. Let’s sketch out
;; how we’d do this using function composition. First, we’ll need a way to represent
;; HTTP requests.
(defn append-a [s] (str s "a"))
(defn append-b [s] (str s "b"))
(defn append-c [s] (str s "c"))
(def append-cba (comp append-a append-b append-c))
(append-cba "z")

(def request
  {:headers
    {"Authorization" "auth" "X-RequestFingerprint" "fingerprint"}
  :body "body"
   }
)

(defn check-authorization [request]
  (let [auth-header (get-in request [:headers "Authorization"])]
    (assoc
      request
      :principal
      (if-not (nil? auth-header) "AUser"))))

(defn log-fingerprint [request]
  (let [fingerprint (get-in request [:headers "X-RequestFingerprint"])]
    (println (str "FINGERPRINT=" fingerprint))
    request))

(defn compose-filters [filters]
  (reduce
    (fn [all-filters, current-filter] (comp all-filters current-filter))
    filters))

(def filter-chain (compose-filters [check-authorization log-fingerprint]))

(filter-chain request)


;; Example 4: Partially applied functions
(defn tax-for-state [state amount]
(cond ;; note cond not if
(= :ny state) (* amount 0.0645)
(= :pa state) (* amount 0.045)))
(def ny-tax (partial tax-for-state :ny)) ;; Note only the arguments that are being partially applied come at  the beginning of the arg-vector
(def pa-tax (partial tax-for-state :pa))

;; PATTERN 17: MEMOIZATION
;; To cache the results of a pure function call to avoid performing the same computation more than once
;; Use memoize keyword

;; PATTERN 18: LAZY SEQUENCE
;; Most of Clojure's core seq manipulation is done in a lazy manner, e.g. range and repeatedly functionss
;; https://clojuredocs.org/clojure.core/repeatedly
;; Also, In Clojure, we can construct an instance of Lazy Sequence from scratch using
;; lazy-sequence and add to it with cons, as shown in the following snippet:
;; => (cons 1 (lazy-seq [2]))

;; PATTERN 19: FOCUSED MUTABILITY
;; Let us become Clojure ninja's before we try this


;; PATTERN 20: CUSTOMIZED CONTROL FLOW
;; Method 1: Use HoFs
(defn choose [num first second third]
  (cond
    (= 1 num) (first)
    (= 2 num) (second)
    (= 3 num) (third)))
(choose 2
  (fn [] (println "hello, world"))
  (fn [] (println "goodbye, cruel world"))
  (fn [] (println "meh, indifferent world")))

;; Method 2: Clojure Macros: TBD in greater detail
