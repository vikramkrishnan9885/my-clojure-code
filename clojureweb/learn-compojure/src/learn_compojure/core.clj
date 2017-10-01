(ns learn-compojure.core
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]))


(defroutes myapp ;; defroutes is a compojure macro that creates a ring handler
  (GET "/" [] "Hello from Compojure")
  (POST "/" [] "Posted to / boilerplate") ;; you can define all rest methods thusly
  ;;(PUT "/" [] "Replace something") ;; need to add method to refer
  ;;(PATCH "/" [] "Modify Something") ;; need to add method to refer
  (DELETE "/" [] "Annihilate something")
  ;;(OPTIONS "/" [] "Appease something") ;; need to add method to refer
  ;;(HEAD "/" [] "Preview something") ;; need to add method to refer

  (POST "/:name" [name] (str "Hello, " name)) ;; POST "/route/:variable" and then pass that variable as an argument to [argument] and use it in your function
  (POST "/abc/:name"  req (str req)) ;;We can use the request directly
  (POST "/xyz/:name" [] (fn [req] (str "Hello, " (-> req :route-params :name))))

  (GET "/route1" [] "Hello from Vikram")
  (GET ["/file/:name.:ext" :name #".*", :ext #".*"] [name ext] (str "File: " name " Extension: " ext)) ;; You can adjust what each parameter matches by supplying a regex

  ;; Returning a response map
  (GET "/hello" []
    {:status 200 :body "Hello World"})
  (GET "/is-403" []
    {:status 403 :body ""})
  (GET "/is-json" []
    {:status 200 :headers {"Content-Type" "application/json"} :body "{}"})

  (context "/admin" []
    (GET "/login" [] "logging in")
    (GET "/logoout" [] "Log Out")
  ) ;; contexts - group similar pages

  ;; (route/resources "/static") ;; Calls to static fetch files from resources/public
  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>") ;; default 404. Note this has to be the last route you specify as this is a default catch all

)

;; You can use this for testing and debugging
(myapp {:uri "/" :request-method :get})
(myapp {:uri "/" :request-method :post})
(myapp {:uri "/" :request-method :delete})
(myapp {:uri "/steve" :request-method :post})
(myapp {:uri "/abc/steve" :request-method :post})
(myapp {:uri "/xyz/steve" :request-method :post})
(myapp {:uri "/file/example.csv" :request-method :get})
;; (myapp {:uri "/static/test.txt" :request-method :get})
(myapp {:uri "/hello" :request-method :get})
(myapp {:uri "/is-json" :request-method :get})
(myapp {:uri "/admin/login" :request-method :get})


(defn -main []
  (jetty/run-jetty myapp {:port 3000}))
