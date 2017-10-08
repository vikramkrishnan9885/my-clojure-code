(ns understanding-core-async.Lesson-HTTP-Async
  (:require [org.httpkit.client :as http]
            [clojure.core.async :refer [chan <! >! go put! <!! >!!]]
            [cheshire.core :as cheshire]))

(defn http-get [url] ;; accept url
  (let [c (chan)] ;; create channel
    (println url) ;; print url for debugging
    (http/get url
              ;; (fn [r] (put! c r)) ;; callback that puts the result on the channel
              (partial put! c)) ;; http kit provides an api that expects a callback
    c)) ;; return channel

(defn request-and-process [nm]
  (go
    (-> (str "http://imdbapi.poromenos.org/js/?name=%25" nm "%25")
        http-get
        <!
        :body
        (cheshire/parse-string true)))) ;; get json return map. True tells cheshire that we don't want string keys we want keyword keys

(<!! (request-and-process "Matrix"))
