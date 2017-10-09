(ns understanding-core-async.Lesson-Dataflow
  (:require [clojure.data.xml :as xml]
            [clojure.core.async :refer [chan <! >! go put! <!! >!!]]
            [org.httpkit.client :as http]
            [clojure.core.async :as async]
            [clojure.java.io :as jio])
  (:import [javax.imageio ImageIO]
           [java.io InputStream ByteArrayOutputStream]
           (java.awt.image BufferedImage)))

(def url "https://api.flickr.com/services/feeds/photos_public.gne") ;; Flickr Public API
(def data (xml/parse (java.io.StringReader.
                       (slurp url)))) ;; slurp grabs the api and xml/parse parses the xml string returned

(def entries (keep (fn [node]
                     (when (= (:tag node) :entry)
                       (println (map :tag (:content node)))
                       {:url   (:href (:attrs (first (filter #(= "enclosure" (-> % :attrs :rel)) (:content node)))))
                        :title (:content (first (filter #(= :title (:tag %)) (:content node))))}))
                   (:content data))) ;; entries returns a hashmap with urls and titles

(vec entries) ;; create a vector of entries

(let [log-c (chan 1024)]
  (go (loop []
        (when-some [v (<! log-c)]
          (println v)
          (recur))))
  (defn log [& itms]
    (>!! log-c (apply str (interpose " " itms))))) ;; Logger is a fairly large channel and you log by taking messages out one at a time
;; the log function will take an arbitrary number of arguments (note the &) , convert it into space-separated string and send it to the logging channel


(defn downloader [in out threads]
  (let [f (fn [{:keys [url] :as data} out-chan] ;; the async function expects to get a hashmap that has the key url
            (log "Downloading " url) ;; you use the function created previously to log the url being downloaded
            (http/get url (fn [response] ;; we use http-kit client to handle the gets the response from the url
                            (put! out-chan (assoc data
                                             :body (:body response))) ;; add a body key to body of the response
                            (async/close! out-chan))))] ;; image will be available as a byte array
    (async/pipeline-async threads out f in))) ;; we use an async pipeline that accepts a number of threads, output, input and an async function f that actually manages the download


(defn image-loader [in out threads] ;; note that the breakup happens on task boundaries. Note that the argument list in all cases is nearly the same
  (let [f (fn [{:keys [^InputStream body url] :as data}] ;; https://clojure.org/reference/reader#_metadata
            (log "Reading..." url)
            (-> data
                (assoc :image (ImageIO/read body)) ;; associate the image reader version of the body with :image and diassociate the body so as to keep the size reasonable
                (dissoc :body)))]
    (async/pipeline threads out (map f) in)))  ;; note the use of pipeline for what is a CPU bound task

(defn resizer [in out threads sizes]
  (let [f (fn [{:keys [^BufferedImage image url] :as data}] ;; 1.  pull out java image and url
            (mapv (fn [[width height :as size]] ;;3.  map over all images with new width and height
                    (let [baos (ByteArrayOutputStream.) ;; 2. create a byte array stream
                          resized-image (BufferedImage. width height BufferedImage/TYPE_INT_ARGB) ;; 4. create a new buffered image
                          g (.createGraphics resized-image)] ;; 5. create a new graphics image
                      (log "Resizing" url width height) ;; 6. log what you are about to do
                      (.drawImage g image 0 0 width height nil) ;; 7. we then draw the original image resized onto the new image size
                      (.dispose g) ;; 8. dispose the graphics object
                      (ImageIO/write resized-image "PNG" baos) ;; 9. write the image out to the byte array
                      (assoc data :image (.toByteArray baos) ;; associate image back
                                  :size size)))
                  sizes))]
    (async/pipeline threads out (mapcat f) in))) ;; mapcat expand input to multiple outputs

(defn write-to-file [name data] ;; take a name and byte array and write to disc
  (with-open [w (jio/output-stream name)]
    (.write w data)))

(defn writer [in out threads] ;; parallelize the previous operation
  (let [f (fn [{:keys [image size url] :as data}]
            (let [[width height] size
                  filename (munge (str url "_" width "_" height ".png"))] ;; munge converts unsafe characters or unacceptable Java class to acceptable equivalents
              (write-to-file filename image) ;; write file to disc
              (-> data
                  (dissoc :image) ;; remove image and associate filename with data
                  (assoc :filename filename))))]
    (async/pipeline-blocking threads out (map f) in))) ;; pipeline blocking because IO operation

(time ;; print how much time it takes
  (let [urls (chan 20) ;; create several channels
        image-bytes (chan 20 (remove (comp string? :body))) ;; contains byte arrays - transducer because error string instead of byte array can be returned if no image. This transducer then removes this
        images (chan 20) ;; contains java images
        resized (chan 20)
        written (chan 20)]

    (async/onto-chan urls entries)
    (downloader urls image-bytes 20)
    (image-loader image-bytes images 2)
    (resizer images resized 8 [[128 128]
                               [256 256]])
    (writer resized written 5)
    (println (<!! (async/into [] written)))
    (println "Done"))) ;; note that benefits of this method is that you can play with the configuration of parallelism in one place and tune performance without editing functions. You can also replace one piece at a time e.g. write to db instead of disc
