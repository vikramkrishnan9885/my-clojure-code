(ns testing-clojure-spec.core)

(require '[clojure.spec.alpha :as s])
(require '[clojure.spec.gen.alpha :as gen])

 (import java.util.Date)

(defn -main []
  (println "Hello World!")

  (println (s/conform even? 1000)) ;; The conform function takes something that can be a spec and a data value. Here we are passing a predicate which is implicitly converted into a spec. The return value is "conformed". Here, the conformed value is the same as the original value - we’ll see later where that starts to deviate. If the value does not conform to the spec, the special value :clojure.spec.alpha/invalid is returned.
  (println (s/conform even? 1001))
  (println (s/valid? even? 1001)) ;; the helper valid? can be used instead to return a boolean.

 ;; Let us now test some further things
  (println (s/valid? nil? nil))

  ;; we can do type checking
  (println (s/valid? string? "abc"))
  (println (s/valid? inst? (Date.)))

  ;; Use it with anonymous functions
  (println (s/valid? #(> % 5) 10))



;; Sets can also be used as predicates that match one or more literal values:

(println (s/valid? #{:club :diamond :heart :spade} :club))
(println (s/valid? #{:club :diamond :heart :spade} 42))

(println (s/valid? #{42} 42))


;; Registry - this is where things start to get interesting
  (s/def ::suit #{:club :diamond :heart :spade})
  (println (s/valid? ::suit :club))
  ;;Once a spec has been added to the registry, doc knows how to find it and print it as well:
  ;; (doc ::suit) This didn't work ;; Of course it didn't apparently doc is in the API for clojure.repl

  ;; Generative testing
  (println (gen/generate (s/gen ::suit)))
  (println (gen/sample (s/gen ::suit)))

  ;; Composing predicates using s/and and s/or
  (s/def ::big-even (s/and int? even? #(> % 1000)))
  (println ["Is this valid" (s/valid? ::big-even :foo)]) ;; This is how you can print a message
  (s/valid? ::big-even 10) ;; false
  (s/valid? ::big-even 100000) ;; true

  ;; Explain this is super awesome
  (println (s/explain ::suit 42))
  ;; The parts of each error are:
  ;;  val - the value in the user’s input that does not match
  ;;  spec - the spec that was being evaluated
  ;;  at - a path (a vector of keywords) indicating the location within the spec where the error occurred - the tags in the path correspond to any tagged part in a spec (the alternatives in an or or alt, the parts of a cat, the keys in a map, etc)
  ;;  predicate - the actual predicate that was not satisfied by val
  ;;  in - the key path through a nested data val to the failing value. In this example, the top-level value is the one that is failing so this is essentially an empty path and is omitted.

  ;; Entity Maps - THIS IS REALLY IMPORTANT

  (def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
  (s/def ::email-type (s/and string? #(re-matches email-regex %)))
  (s/def ::acctid int?)
  (s/def ::first-name string?)
  (s/def ::last-name string?)
  (s/def ::email ::email-type)
  (s/def ::person (s/keys :req [::first-name ::last-name ::email]
                        :opt [::phone]))



(s/valid? ::person
  {::first-name "Elon"
   ::last-name "Musk"
   ::email "elon@example.com"})
;;=> true

;; Fails required key check
(s/explain ::person
  {::first-name "Elon"})
;; val: #:my.domain{:first-name "Elon"} fails spec: :my.domain/person
;;  predicate: (contains? % :my.domain/last-name)
;; val: #:my.domain{:first-name "Elon"} fails spec: :my.domain/person
;;  predicate: (contains? % :my.domain/email)


;; Fails attribute conformance
(s/explain ::person
  {::first-name "Elon"
   ::last-name "Musk"
   ::email "n/a"})
;; In: [:my.domain/email] val: "n/a" fails spec: :my.domain/email-type
;;   at: [:my.domain/email] predicate: (re-matches email-regex %)

;; Let’s take a moment to examine the explain error output on that final example:
;;    in - the path within the data to the failing value (here, a key in the person instance)
;;    val - the failing value, here "n/a"
;;    spec - the spec that failed, here :my.domain/email
;;    at - the path in the spec where the failing value is located
;;    predicate - the predicate that failed, here (re-matches email-regex %)


  (defrecord Person [first-name last-name email phone])
  (s/def :unq/person
  (s/keys :req-un [::first-name ::last-name ::email]
          :opt-un [::phone]))
  (println (s/explain :unq/person
           (->Person "Elon" nil nil nil)))
)
