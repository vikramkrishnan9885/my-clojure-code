(ns learn-io)

(def my-file-path "/home/vikram/Dropbox/udemy/LearningClojure/my-clojure-code/learn-io-files/tmp.txt")

;; Lisp style file io
(spit my-file-path "Hello")
(slurp my-file-path)

;; Java style IO
(require '[clojure.java.io :as io])
(def reader (io/reader my-file-path)) ;; create a reader and assign a name
(def lines (line-seq reader)) ;; This is a lazy reader. If you close reader before you extracted the lines, you can lose your lines
(print lines) ;; prints lines to stdout
(.close reader)

;; use with-open to autoclose files
(with-open [reader (io/reader my-file-path)]
  (println (line-seq reader)))

;; look at .write method of writer and doall

;; slurp works with urls as well
(slurp "http://google.com/")