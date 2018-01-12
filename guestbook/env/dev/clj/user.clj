(ns user
  (:require [luminus-migrations.core :as migrations]
            [guestbook.config :refer [env]]
            [mount.core :as mount]
            guestbook.core))

(defn start []
  (mount/start-without #'guestbook.core/repl-server))

(defn stop []
  (mount/stop-except #'guestbook.core/repl-server))

(defn restart []
  (stop)
  (start))

(defn migrate []
  (migrations/migrate ["migrate"] (select-keys env [:database-url])))

(defn rollback []
  (migrations/migrate ["rollback"] (select-keys env [:database-url])))


