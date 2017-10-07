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


;; PARAMETERIZED GET

(
  defn get-project [request]
    (
      let [projname (get-in request [:path-params :project-name])] ;; HOWEVER, GETTING NESTED VALUES IS COMMON ENOUGH THAT CLOJURE PROVIDES A FUNCTION DESIGNED SPECIFICALLY TO ADDRESS THAT NEED: GET-IN. THE GET-IN FUNCTION RETURNS THE VALUE IN A NESTED ASSOCIATIVE STRUCTURE WHEN GIVEN A SEQUENCE OF KEYS. USING GET-IN YOU CAN REPLACE THE LAST EXAMPLE WITH THE FOLLOWING CODE. (-> json-data read-json (get-in ["scores" "FSU"])) .. user=> (get-in {"timestamp" 1291578985220 "scores" {"FSU" 31 "UF" 7}} ["scores" "FSU"]) GIVES US 31
        ;; EXAMPLES OF GET-IN
        ;; We can use get-in for reaching into nested maps:
        ;; user=> (def m {:username "sally"
        ;;       :profile {:name "Sally Clojurian"
        ;;                 :address {:city "Austin" :state "TX"}}})
        ;; user=> (get-in m [:profile :name])
        ;; "Sally Clojurian"
        ;; user=> (get-in m [:profile :address :city])
        ;; "Austin"
        ;;
        ;; NOW WHAT ARE WE SELECTING FOR USING GET-IN
        ;; REMEMBER THAT WHEN WE PRINTED OUR REQUEST WE GOT
        ;; INFO  io.pedestal.http.cors - {:msg "cors request processing", :origin "", :allowed true, :line 84}
        ;; {:protocol "HTTP/1.1", :async-supported? true, :remote-addr "127.0.0.1", :servlet-response #object[org.eclipse.jetty.server.Response 0xf2e0986 "HTTP/1.1 200 \nDate: Sat, 07 Oct 2017 14:53:15 GMT\r\n\r\n"], :servlet #object[io.pedestal.http.servlet.FnServlet 0x509e22f4 "io.pedestal.http.servlet.FnServlet@509e22f4"], :headers {"user-agent" "curl/7.52.1", "accept" "*/*", "host" "localhost:5000", "origin" ""}, :server-port 5000, :servlet-request #object[org.eclipse.jetty.server.Request 0x3158fdeb "Request(GET //localhost:5000/)@3158fdeb"], :path-info "/", :url-for #object[clojure.lang.Delay 0xfee9e05 {:status :pending, :val nil}], :uri "/", :server-name "localhost", :query-string nil, :path-params [], :body #object[org.eclipse.jetty.server.HttpInputOverHTTP 0x6448b75f "HttpInputOverHTTP@6448b75f[c=0,q=0,[0]=null,s=STREAM]"], :scheme :http, :request-method :get}
        ;; WE ARE TRYING TO EXTRACT PROJECT-NAME FROM THE PATH-PARAMS
        ;;
        (
          http/json-response ((keyword projname) mock-project-collection)
          ;; KEYWORD Returns a Keyword with the given namespace and name.
          ;; FOR EXAMPLE
          ;; user=> (keyword 'foo)
          ;; :foo
        )
    )
)

;; Defines "/" and "/about" routes with their associated :get handlers.
;; The interceptors defined after the verb map (e.g., {:get home-page}
;; apply to / and its children (/about).
(def common-interceptors [(body-params/body-params) http/html-body])

;; Tabular routes
(def routes #{["/" :get (conj common-interceptors `home-page)]
              ["/projects" :get (conj common-interceptors `get-projects)] ;; ADD OUR GET ENDPOINT, NEED FURTHER STUDY TO SEE HOW THE CODE BELOW DIFFERS FROM THE TABLE THINGIE WE HAVE HERE
              ["/projects/:project-name" :get (conj common-interceptors `get-project)] ;; END POINT FOR PARAMETERIZED GET
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

