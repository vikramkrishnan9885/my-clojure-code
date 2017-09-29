;; This was the boiler plate leiningen created. We will instead write our own tests
;; (ns learnclojure.core-test
;;  (:require [clojure.test :refer :all]
;;            [learnclojure.core :refer :all]))

;; (deftest a-test
;;  (testing "FIXME, I fail."
;;    (is (= 0 1))))

(ns learnclojure.core-test
  (:require [clojure.test :refer [is deftest testing]])
)

;; Let us now create our own tests with the deftest macro

(deftest my-test
  (testing "Our message"
    (is (= (+ 1 1) 1))
  )

  ;; Note that within one deftest we can create multiple assertions

  (testing "One plus One is two"
    (is (= (+ 1 1) 2))
  )

)
