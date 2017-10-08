(ns lesson-0201
  (:refer-clojure  :exclude [merge])) ;;  otherwise if you use merge from core.async warnings will be thrown
(require '[clojure.core.async :refer [chan >!! <!! >! <! put! take! close! sliding-buffer dropping-buffer thread go alt!! alts!! merge mult tap pub sub] :as async])

(chan) ;; Channel permits multipler readers and writers. Channel ensures that race  conditions etc don't occur by ensuring proper blocking

(let [c (chan)]
  (future (>!! c 42)) ;; First ! tells us there wil be side-effects, Second ! tells us that operation is blocking the thread
  (future (println (<!! c))) ;; Now we create a new future on a new thread and print value from channel
) ;; Note that channels are FIFO and asynchronous

(let [c (chan)]
  (future (dotimes [x 10] ;; This is how you create loops that run a fixed number of times
            (>!! c x)))
  (future (dotimes [x 10]
            (println (<!! c))))
) ;; Note that because it is FIFO, order is preserved

(let [c (chan)]
  (put! c 42)
  (take! c (fn [v] (println "Got " v)))) ;; Note the callback, otherwise it will give an error

(let [c (chan)]
  (put! c 42 (fn [v] (println "Sent " v))) ;; Optional callback in put!
  (take! c (fn [v] (println "Got " v)))) ;; Note that the put callback prints after the take callback. This is because put! cannot succeed till take! happens. Implying that it is in this form i.e. vanilla chan is purely a handoff mechanism between threads

(let [c (chan 1)]
  (future
    (dotimes [x 3]
      (>!! c x)
      (println "Sent: " x)))
  (future
    (dotimes [x 3]
      (println "Got: " (<!! c))))
)

(let [c (chan (dropping-buffer 1))] ;; When full drop new values - where as sliding-buffer drops old values
  @(future ;; Dereference to ensure that it executes before the takes are called
    (dotimes [x 3]
      (>!! c x)
      (println "Sent: " x)))
  (future
    (dotimes [x 3]
      (println "Got: " (<!! c))))
)


(let [c (chan)]
  (future
    (dotimes [x 2]
      (>!! c x))
    (close! c) ;; Close signals deliver any pending puts and then deliver nil. Buffers will be flushed before close! is executed
    (println "Closed."))
  (future
    (loop []
      (when-some [v (<!! c)] ;; when-some - perform an operation and give it a value. If value is nil don't execute, otherwise execute
        (println "Got: " v)
          (recur)))
    (println "Exiting")))


(thread 42) ;; Thread macro executes the body within thread it created. It takes value produced and writes to channel produced by thread macro
(<!! (thread 42))

(<!! (thread ;; Note that threads are rather heavy and need 1-2MB of memory. However, it can be used for long running jobs and if we are spinning up only a few at a time
       (let [t1 (thread "Thread 1")
             t2 (thread "Thread 2")] ;; Nested threads spawned by parent thread. These threads are executed in parallel
         [(<!! t1)
          (<!! t2)] ;; take values from channel and insert in macro
       )
      )
)
;; core.async prefers thread over future because thread returns a channel that can be used with other core.async functions, whereas future returns a promise


;; go macro provides an easy way to create and manage lightweight "threads". These are not OS or JVM threads.
;; Go macro takes the code in body of macro, chops into small pieces and composes it in a series of callbacks that can be used by put and take
;; Thus resource requirements are similar to callbacks without resulting in callback hell
(go 42) ;; creates m-2-m channel
(<!! (go 42))

(let [c (chan)]
  (go (dotimes [x 3]
        (>! c x)
        (println "Put: " x)))
  (go (dotimes [x 3]
        (println "Take " (<! c))))
)
;; When inside go block use put and take parking. the parking bit tells the go macro where to split the code

(clojure.pprint/pprint (macroexpand '(go (>! (chan) 42)))) ;; See the callback hell you avoided

;; Caveats:
;; Never use the blocking versions of take and put
;; go block stops translation at call to fn. Use doseq instead of fn

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Blocking IO example
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def logging-chan (chan 24))

(future
  (loop [] ;; take values one at a time from logging-chan
    (when-some [v (<!! logging-chan)] ;; when-some closes when we are done
      (println v)
        (recur)))) ;; note this loop pattern

(defn log [& args]
  (>!! logging-chan (apply str args)))

(do
  (future
      (dotimes [x 100]
        (println "(... " x " ...)")))
  (future
    (dotimes [x 100]
      (println "(... " x " ...)")))
)

(do
  (future
      (dotimes [x 100]
        (log "(... " x " ...)")))
  (future
    (dotimes [x 100]
      (log "(... " x " ...)")))
)

;; Note some facts: You have two threads that are trying to access the same resource. We use CSP to resolve access issues by having:
;; (1) the processes communicate with each other using channels
;; (2) single thread manages the shared resource
;; Hence Communicating Sequential Processes


;; Backpressure

(def c (chan 24))

(go
  (loop [i 0]
    (println "Putting " i)
    (>! c i)
    (recur (inc i))))

(<!! c)

;; It is useful to think of this kind of modeling like an assembly line at an auto manufacturing store
;; Your producers are working at a certain rate
;; And your consumers are working at a different rate
;; There could also be different number of producers and consumers
;; And the channel is the conveyer belt that connects them

;; We can use this to create dataflow style programming

(defn map-pipe [in out f]
  ;; write some body here
  (go (loop []
       (when-some [v (<! in)]
          (>! out (f v))
            (recur)))
  (close! out)))

(defn map-pipe-1
  (
    [in out f] ;; One type signature
    (map-pipe-1 0 in out f) ;; Use this pattern when you want to have similar inputs with some default values
  )
  ([p in out f] ;; Another type signature
   (dotimes [_ p] ;; Parallelize operations
    (go (loop []
         (when-some [v (<! in)]
            (>! out (f v))
              (recur)))
    (close! out))))
)

(let [in (chan 1)
      a (chan 1)
      b (chan 1)
      c (chan 1)
      out (chan 1)] ;; adding 1 doubles the number of values in the system. In prod you can go much higher ~ 1024 if you have the RAM
  (map-pipe in a step-a)
  (map-pipe a b step-b)
  (map-pipe-1 2 b c step-c)
  (map-pipe c out step-d)) ;; Max number of values that can be in the system at a given time is 4 one for each step. To improve performance, add buffers in each channel . IRL the size of the buffer should depend on time take for a step to the next
;; put! and >! should always have some kind of callback, to avoid backpressure


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; alts! and alt!
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; select value from one or the other channel

(let [c1 (chan 1)
      c2 (chan 1)]
  (>!! c1 42)
  (>!! c2 44)
  (thread
    ;; (println (alts!! [c1 c2]))
    (let [[v c] (alts!! [c1 c2])]
      (println "Value: " v)
      (println "Chan1? " (= c1 c))
      (println "Chan2? " (= c2 c))
                       ))) ;; alts!! selects one of a vector of channels. It returns value and name of channel
;; Note that only one value is selected at random.
;; General rule !! for blocking, typically inside a thread and ! inside go block


;; alts!! followed by a case to determine which channel value is taken from is common enough that it has a macro called alt!!
(let [c1 (chan 1)
      c2 (chan 1)]
  ;;(>!! c1 42)
  ;;(>!! c2 44)
  (thread
    (println (alt!! [c1] :first
                    [c2] :second
                    :default :the-default-val)))) ;; dont forget to specify :default to ensure that thread is not blocked if not values being put on channel


(let [c1 (chan 1)
      c2 (chan 1)]
  (thread
    (let [[v c] (alts!! [c1 [c2 42]])] ;; You can use alts!! to add values to a channel
      (println "Value: " v)
      (println "Chan1? " (= c1 c))
      (println "Chan2? " (= c2 c))
                       )))


;; Channel priority with alts!
;; Example use case: admin user could be on one channel and non-admin users on another channel

(let [c1 (chan 1)
      c2 (chan 1)]
  (>!! c1 42)
  (>!! c2 44)
  (thread
    (let [[v c] (alts!! [c1 c2] :priority true)] ;; note that priority means that it will only try the operation in that order. It wil not wait for the operation to be done in that order
      (println "Value: " v)
      (println "Chan1? " (= c1 c))
      (println "Chan2? " (= c2 c))
                       )))

;; merge

(let [c1 (chan 10)
      c2 (chan 10)
      cm (merge [c1 c2] 25)]

(>!! c1 13)
(>!! c2 12)
(>!! c1 43)
(>!! c2 32)
(close! c1)
(close! c2) ;; cm will close when c1 and c2 are closed

(dotimes [x 4]
  (println (<!! cm))))

;; mult - broadcast single value to multiple channels

(let [c (chan 10)
      m (mult c)
      t1 (chan 10)
      t2 (chan 10)]
  (tap m t1)
  (tap m t2)

  (>!! c 42)

  (thread
    (println "got from t1: " (<!! t1)))

  (thread
    (println "got from t2: " (<!! t2))))

;; pub sub is an api in core async that we use when we want to distribute values across the syatem, but filter the value based on some criteria
;; Used for categorizing data
;; Combine with filters or multimethods

(let [c (chan)
      p (pub c pos?)
      s1 (chan 10)
      s2 (chan 10)
    ]
  (sub p true s1) ;; topic values true and false
  (sub p false s2)

  (>!! c 43)
  (>!! c -56)
  (>!! c -2)
  (>!! c 32)

  (thread
    (println "s1:" (<!! s1))
    (println "s1:" (<!! s1)))

  (thread
    (println "s2:" (<!! s2))
    (println "s2:" (<!! s2)))
  )


;; reduce and into

(let [c (chan)]
  (async/onto-chan c (range 10))
  (<!! (async/reduce conj [] c))) ;; you can use any reducing function here

(let [c (chan)]
  (async/onto-chan c (range 10))
  (<!! (async/into #{} c))) ;; into - collection
