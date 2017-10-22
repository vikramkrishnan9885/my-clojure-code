(ns testing-clojure-spec.core)

(require '[clojure.spec.alpha :as s])

(defn -main []
  (println "Hello World!")

  (println (s/conform even? 1000)) ;; The conform function takes something that can be a spec and a data value. Here we are passing a predicate which is implicitly converted into a spec. The return value is "conformed". Here, the conformed value is the same as the original value - we’ll see later where that starts to deviate. If the value does not conform to the spec, the special value :clojure.spec.alpha/invalid is returned.
  (println (s/conform even? 1001))
)
