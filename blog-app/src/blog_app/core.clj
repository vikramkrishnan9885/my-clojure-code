(ns blog-app.core
  (:require [net.cgrand.enlive-html :as enlive])))

(defrecord Tweed [title content]) ;; This is how you define models. We will then define a protocol that will describe the functions that operate on the record

(defprotocol TweedStore ;; Needs further research on the UD of CRUD, do we not do that to maintain immutability? Or do we return a different object
  (get-tweeds [store])
  (put-tweed! [store tweed])) ;; ! denotes side-effects

(defrecord AtomStore [data]) ;; So persistence is another record for which we extend the given protocol


(extend-protocol TweedStore
  AtomStore ;; Extend protocol for which record
  (get-tweeds [store]
    (get deref(:data store) :tweeds)) ;; This needs further investigating
  (put-tweed! [store tweed]
    (swap! (:data store)
           update-in [:tweeds] conj tweed))) ;; So does this

;; Let us test this out

(def store (->AtomStore (atom {:tweeds '()}))) ;;  Crreate instance oof AtomStore and listt of tweeds becaause we want to omly add to beginning
(get-tweeds store)
(put-tweed! store (->Tweed "Test" "test content"))
