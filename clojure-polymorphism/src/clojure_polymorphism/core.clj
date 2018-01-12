(ns clojure-polymorphism.core)

;; Clojure approaches to polymorphic dispatch
;; defmulti: multimethods are the most general form of polymorphic dispatch. Performance is less than defprotocol, but this function can run arbitrary code
;; defprotocol:will dispatch on type of its first arg
;; reify: implement protocols and interfaces as anonymous class.  Unable to define fields, but it is a closure and you can capture an atom
;; deftype: Implements several protocols. Can include mutable fields
;; defrecord: creates new immutable, map like data structure. Not a closure, but can be used nearly exactly as a map. Similar to Scala's case classes

;; EXAMPLE 1: SERVICE ABSTRACTION
;; Polymorphism can be used to create a service abstraction , for e..g a storage service abstraction modeled as a key-value store for binary objects. 

