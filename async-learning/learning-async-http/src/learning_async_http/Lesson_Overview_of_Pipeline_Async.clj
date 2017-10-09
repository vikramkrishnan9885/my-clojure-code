(ns understanding-core-async.Lesson-Overview-of-Pipeline-Async
  (:require [org.httpkit.client :as http]
            [clojure.core.async :refer [chan <! >! go put! <!! >!! pipeline-async
                                        close!]]
            [cheshire.core :as cheshire]))

(defn http-get [url c] ;; add response channel to the argument list. So no longer need  let
  (println url)
  (http/get url (fn [v]
                  (put! c v)
                  (close! c))) ;; Closing the channel to signal we are done putting data on the channel
  c)
;; Note that relative to the vanilla version, here the functionality is implemented using transducers
(let [from (chan 10 (map (fn [name]
                           (str "http://imdbapi.poromenos.org/js/?name=%25"
                                name
                                "%25")))) ;; create url from movie name

      to (chan 10 (map (fn [response] ;; to channel parses body
                         (-> response
                             :body
                             (cheshire/parse-string true)))))] ;; not the use of map + function. Also note the to and from
  (pipeline-async 10 to http-get from) ;; parallelism number, to, an async function and from channel - note function should be async
  ;; pipeline-async has a very specific contract in  its async function, it comprises of an item it will take from the channel and a response channel to which it will put! its output
  ;; it is the responsibility of the async function is to close the channel, otherwise the channel can fail
  (>!! from "Home")
  (println (<!! to)))


