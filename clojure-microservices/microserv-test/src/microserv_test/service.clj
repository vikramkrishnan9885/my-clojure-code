(ns microserv-test.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp])) ;; ALIASES IMPROVE READABILITY


;; WE ARE MOCKING A JSON OBJECT RESPONSE
(def mock-project-collection
  {
    :sleeping-cat
    {
      :name "Sleeping cat"
      :framework "Play"
      :language "Scala"
      :repo "http://www.google.co.uk"
    }
    :stinky-dog
    {
      :name "Stinky Dog Experiment"
      :framework "Pedestal"
      :language "Clojre"
      :repo "No rep"
    }
  }
)



;; WHEN FUNCTION AB0UT-PAGE IS INVOKED, IT IS PASSED AN ARGUMENT CALLED REQUEST AND THE REMAINDER OF THE FUNCTION IS THE CODE TO EVAULATE THE FUNCTION AND RETURN A RESPONSE
;; RESPONSE BEING RETURNED IS A STRING THAT IS FORMATTED AND TAKES TWO ARGUMENTS (SIMILAR TO .FORMAT IN PYTHON) ARG 1 IS THE VERSION OF CLOJURE AND ARG 2 IS THE URL FOR THE ABOUT PAGE
(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn home-page
  [request]
  (prn request) ;; WE WANT TO PRINT THE REQUEST TO THE COMMAND LINE. NOTE THIS HAS SIDE-EFFECTS
  (ring-resp/response "Hello World from local!"))

(defn get-projects ;; FUNCTION THAT WE HAVE MENTIONED IN TEH TABULAR ROUTES
  [request]
  (http/json-response mock-project-collection)) ;; THIS ENSURES THAT WE GET A JSON RESPONSE


;; Defines "/" and "/about" routes with their associated :get handlers.
;; The interceptors defined after the verb map (e.g., {:get home-page}
;; apply to / and its children (/about).
(def common-interceptors [(body-params/body-params) http/html-body])

;; Tabular routes
(def routes #{["/" :get (conj common-interceptors `home-page)]
              ["/projects" :get (conj common-interceptors `get-projects)] ;; ADD OUR GET ENDPOINT, NEED FURTHER STUDY TO SEE HOW THE CODE BELOW DIFFERS FROM THE TABLE THINGIE WE HAVE HERE
              ["/about" :get (conj common-interceptors `about-page)]})

;; Map-based routes
;(def routes `{"/" {:interceptors [(body-params/body-params) http/html-body]
;                   :get home-page
;                   "/about" {:get about-page}}})

;; Terse/Vector-based routes
;(def routes
;  `[[["/" {:get home-page}
;      ^:interceptors [(body-params/body-params) http/html-body]
;      ["/about" {:get about-page}]]]])


;; Consumed by microserv-test.server/create-server
;; See http/default-interceptors for additional options you can configure
;; CREATES A MAP THAT PEDESTAL USES TO RUN YOUR APP
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::http/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::http/allowed-origins ["scheme://host:port"]

              ;; Tune the Secure Headers
              ;; and specifically the Content Security Policy appropriate to your service/application
              ;; For more information, see: https://content-security-policy.com/
              ;;   See also: https://github.com/pedestal/pedestal/issues/499
              ;;::http/secure-headers {:content-security-policy-settings {:object-src "'none'"
              ;;                                                          :script-src "'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:"
              ;;                                                          :frame-ancestors "'none'"}}

              ;; Root for resource interceptor that is available by default.
              ::http/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ;;  This can also be your own chain provider/server-fn -- http://pedestal.io/reference/architecture-overview#_chain_provider
              ::http/type :jetty
              ::http/host "localhost"
              ;; ::http/port 8080 ;; YOU DON'T WANT THIS DEFAULT IF YOU WANT TO RUN THE APP ON HEROKU ETC. HENCE WE HAVE COMMENTED IT OUT
              ::http/port (Integer. (or (System/getenv "PORT") 5000)) ;; GET PORT FROM SYSTEM ENV VARIABLES OR SET TO 5000 AS A DEFAULT
              ;; Options to pass to the container (Jetty)
              ::http/container-options {:h2c? true
                                        :h2? false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                        :ssl? false}})

