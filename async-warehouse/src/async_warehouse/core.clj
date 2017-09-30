(ns async-warehouse.core
  (:gen-class)
  (:require [clojure.core.async :as async :refer [ >! <! >!! <!! go chan go-loop buffer close! alts!!]]) ;; Import the functions and macros you need
)

(def warehouse-capacity 10) ;; will give us a channel with a buffer size of 10. This means that we can not store more than 10 things in our channel at once. If we attempt to store more than 10 things, our current thread will block.

(def warehouse-channel (chan warehouse-capacity)) ;; we're going to use something called a channel. A channel is similar to a queue

(def stock-map { 0 :shirt
                 1 :pants
                 2 :socks
                 3 :shoes
               }
)

(defn- generate-random-items []
  (let [items (for [x (range warehouse-capacity)]
      (rand-int (count (keys stock-map))))]
      (map #(get stock-map %) items ))
)

(defn load-items-into-channel [items channel]
  (map #(>!! channel %) items)
)

(defn -main [&args]
  (load-items-into-channel (generate-random-items) warehouse-channel))
