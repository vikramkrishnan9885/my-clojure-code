(ns async_warehouse.async-test)
(require '[clojure.core.async :refer [chan go >! <! go-loop] :as async])

(def my-chan (chan)) ;; create channel and give it a name

(go (println (<! my-chan))) ;; Print whatever you TAKE from the channel

(go (>! my-chan "Hello")) ;; PUT to channel

(go-loop []
  (println (<! my-chan))
  (recur)
) ;; Eternally print takes form the channel  my-chan

(async/>!! my-chan "Hi!") ;; be careful if there are no available listeners this will block the channel

;; Channels can be passed around to functions
;; Note that in such cases it is advisable that the function return the channel
(defn print-listener [chan] ;; this wraps the previous functionality in a function
  (go-loop []
    (println (<! chan))
    (recur)
  )
chan)

(def new-chan (chan))
(print-listener new-chan)
(go  (>! new-chan "Hey"))

(defn reverser [in-chan]
  (let [out-chan (chan)] ;; I noticed one thing that when let [name (arg)] (function-body) is used when name is the output and is typically returned. See core for further details
    (go-loop []
             ;; (>! out-chan (println (reverse (<! in-chan)))) ;; This gives a Java IllegalArgumentException: can't put nil on channel
             (>! out-chan (reverse (<! in-chan)))
             (recur))
    out-chan))

(def in-chan (chan))
(def rev-chan (reverser in-chan))
(go (>! in-chan [1 2 3]))

;; Learn more abt async/map async/reduce etc
