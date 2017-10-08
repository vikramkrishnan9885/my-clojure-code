(ns lesson-0201)
(require '[clojure.core.async :refer [chan >!! <!! >! <! put! take! close! sliding-buffer dropping-buffer thread go]])

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

