(ns async-warehouse.core
  (:gen-class)
  (:require [clojure.core.async :as async :refer [ >! <! >!! <!! go chan go-loop buffer sliding-buffer close! alts!!]]) ;; Import the functions and macros you need. > is called put and < is called take. ! denotes non-blocking, while !! is blocking
)

(def warehouse-capacity 10) ;; will give us a channel with a buffer size of 10. This means that we can not store more than 10 things in our channel at once. If we attempt to store more than 10 things, our current thread will block.

(def warehouse-channel (chan warehouse-capacity)) ;; we're going to use something called a channel. A channel is similar to a queue

(def stock-map { 0 :shirt
                 1 :pants
                 2 :socks
                 3 :shoes
               }
)  ;; Think enum in Java

(defn- generate-random-items []
  (let [items (for [x (range warehouse-capacity)]
      (rand-int (count (keys stock-map))))]
      (map #(get stock-map %) items )) ;; randomlu generate items using the keys in the stock-map
)

(defn load-items-into-channel [items channel]
  (map #(>!! channel %) items) ;; #() is equivlaent to fn [args] () . Also % denotes argument, %1 %2 denotes first and second argument etc
)

;; One of the key differences with the go macro is the ability to use >! and <!. These are only usable within a go block, and they signify that you'd like to park the thread instead of block the thread. If you park the thread, the thread can be relieved to work on other tasks in the mean time. Most of the time, you'll want to use >! and <! when you're in go block instead of >!! and <!!. That being said, you're free to use both types in a go block, too!
;; The reason that the REPL would block is simply because we told it to. We said "take from this channel and block until we get the result" (<!!). If there's nothing to take, we'll block the current thread and wait for something to exist in that channel. Once something does exist, we'll retrieve it from the channel and be unblocked. This is standard async behavior.
;; Parking is available when you aren't adding to or reading from channels on the main thread, but instead delegating these processes to a thread pool. What's the difference exactly? When we were blocked in the REPL earlier, we had locked that thread, and it patiently waited for something to exist in that channel. With Parking, we're able to tell our thread "Hey, wait for this channel to have something to take, but you can do other stuff in the meantime." We can use the go macro to help take care of this for us.
;; Because go blocks use a thread pool with a fixed size, you can create 1,000 go processes but use only a handful of threads

(defn -main [&args]
  (load-items-into-channel (generate-random-items) warehouse-channel))

;; When to use Go blocks at all. Think of this real life example - When you wait for the mail, you're parking, not blocking (unless you stand in front of your mailbox and do nothing until the mail-person arrives). Realistically, you're waiting on the mail to arrive, but you're still able to handle other tasks like vacuuming, eating, or writing code. Once the mail arrives, you retrieve it from the mailbox, and continue what you you were doing before. Writing your async processes in this manner is a very powerful technique.

(defn make-payment-channel []
  (let [payments (chan)]
    (go (while true
      (let [in (<! payments)]
        (if (number? in)
          (do (println (<!! warehouse-channel)))
          (println "We only accept numeric values!")
        )
      )
    ))
  payments) ;; Get channel return channel is a common pattern. It lets us string these functions together and run it
)

;;
;;async-warehouse.core=> (generate-random-items)
;;(:shoes :pants :pants :socks :pants :socks :socks :shoes :shoes :shoes)
;;async-warehouse.core=> (generate-random-items)
;;(:pants :pants :shirt :socks :pants :shoes :pants :pants :shirt :pants)
;;async-warehouse.core=> (make-payment-channel)
;;#object[clojure.core.async.impl.channels.ManyToManyChannel 0x70b5d24e "clojure.core.async.impl.channels.ManyToManyChannel@70b5d24e"]
;;async-warehouse.core=> (load-items-into-channel (generate-random-items) warehouse-channel)
;;(true true true true true true true true true true)
;;async-warehouse.core=>
;;
;;async-warehouse.core=> (def incoming (make-payment-channel))
;;#'async-warehouse.core/incoming
;;async-warehouse.core=>
;;
;;async-warehouse.core=> (>!! incoming 5)
;;true
;;:socks
;;async-warehouse.core=> (>!! incoming :foo)
;;trueWe only accept numeric values!
;;
;;async-warehouse.core=> (>!! warehouse-channel :banana)
;;true
;;async-warehouse.core=>
;;
;; A sliding-buffer follows the first-in-first-out principle. If we were to put 0, 1, and 2 into this channel, and then put 3 into the channel as well, the items in the channel would be 1, 2, and 3. There's also a dropping-buffer, which does the opposite - Last-in-first-out. These buffers are particularly interesting because they never block their current thread on a put. They simply remove an object from the queue if you try to add beyond it's capacity.
;;

(def banana-channel (chan (sliding-buffer 3)))
(>!! banana-channel :banana)

(defn make-payment-channel []
  (let [payments (chan)]
    (go (while true
      (let [in (<! payments)]
        (if (number? in)
          (let [[item ch] (alts!! [warehouse-channel banana-channel])]
            (println item))
          (println "We only accept numeric values! No Number, No Clothes || Bananas!")))))
    payments))

;;
;;async-warehouse.core=> (load-items-into-channel (generate-random-items) warehouse-channel)
;;(true true true true true true true true true true)
;;async-warehouse.core=> (def new-payments (make-payment-channel))
;;#'async-warehouse.core/new-payments
;;async-warehouse.core=>
;;
;;async-warehouse.core=> (>!! new-payments 5)
;;true
;;:banana
;;async-warehouse.core=> (>!! new-payments 5)
;;true:shirt
;;
;;async-warehouse.core=> (>!! new-payments 5)
;;true:pants
;;
;;async-warehouse.core=> (>!! new-payments 5)
;;true:socks
;;
;;async-warehouse.core=> (>!! new-payments 5)
;;:sockstrue
;;
;;async-warehouse.core=> (>!! new-payments 5)
;;true:pants
;;
;;async-warehouse.core=> (>!! new-payments 5)
;;true:shoes
;;
;;async-warehouse.core=> (>!! new-payments 5)
;;true:pants
;;
;;async-warehouse.core=> (>!! new-payments 5)
;;:shirt
;;true
;;async-warehouse.core=> (>!! new-payments 5)
;;:pants
;;true
;;async-warehouse.core=> (>!! new-payments 5)
;;:pants
;;true
;;async-warehouse.core=> (>!! new-payments 5)
;;true
;;async-warehouse.core=> (>!! new-payments 5)
;;
;;async-warehouse.core=> (close! banana-channel)
;;nil
;;nil
;;nil
;;async-warehouse.core=>
;;
;;async-warehouse.core=> (<!! banana-channel)
;;nil
