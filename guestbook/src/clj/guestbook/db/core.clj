(ns guestbook.db.core
  (:require
    [clj-time.jdbc]
    [conman.core :as conman]
    [mount.core :refer [defstate]]
    [guestbook.config :refer [env]]))

(defstate ^:dynamic *db*
           :start (conman/connect! {:jdbc-url (env :database-url)})
           :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/queries.sql")

;;  The guestbook.db.core namespace contains the logic for defining queries 
;; and managing the database connection, while the guestbook.db.migrations 
;; namespace is responsible for managing the migrations logic.