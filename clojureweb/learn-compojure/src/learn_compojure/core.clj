(ns learn-compojure.core
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]))


(defroutes myapp ;; defroutes is a compojure macro that creates a ring handler
  (GET "/" [] "Hello from Compojure")
  (POST "/" [] "Posted to / boilerplate") ;; you can define all rest methods thusly
  (POST "/:name" [name] (str "Hello, " name)) ;; POST "/route/:variable" and then pass that variable as an argument to [argument] and use it in your function
  (POST "/abc/:name"  req (str req))
  (GET "/route1" [] "Hello from Vikram")
  (route/not-found "<h1>Page not found</h1>") ;; default 404
)

;; You can use this for testing and debugging
(myapp {:uri "/" :request-method :get})
(myapp {:uri "/" :request-method :post})
(myapp {:uri "/steve" :request-method :post})
(myapp {:uri "/abc/steve" :request-method :post})

(defn -main []
  (jetty/run-jetty myapp {:port 3000}))
