(ns learn-ring.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty] ;; Ring takes clojure functions and adapts it to work with Java's servlet system. This means that you can run Ring applications on any existing Java web server like Jetty or Tomcat or Glassfish
            [ring.middleware.params :refer [wrap-params]]) ;; it enables us to extract variable values from the url string
)

;; (defn myapp [request]
;;   {
;;     :body "Hello World"
;;     :status 200
;;     :headers {"Content-Type" "text/html"}
;;   } ;; Notice how this is data
;; )
;; Above was the original request handler

;; (defn myapp [request] "Hello World!") ;; Basic request handler

(defn myapp [request]
  (str "Hello, " (get (:params request) "name")))

;;Writing some middleware
(defn string-response-middleware [handler]
  (fn [request]
    (let [response (handler request)]
      (if
        (instance? String response);;Predicate
        {:body response
         :status 200
         :headers {"Content-Type" "text/html"}} ;; If true return response
        response))))

(def handler
  (-> myapp
      string-response-middleware
      wrap-params)) ;; Otherwise the command in the main would get very unweildy
;; Note that the order of middleware functions is important
;; Requests flow up
;; Responses flow down


(defn -main []
 ;; (jetty/run-jetty myapp {:port 3000}) ;; This is the most basic vanila version of the main function
 ;; (jetty/run-jetty (string-response-middleware myapp) {:port 3000}) ;; apply middleware to app
  (jetty/run-jetty handler {:port 3000})
)
