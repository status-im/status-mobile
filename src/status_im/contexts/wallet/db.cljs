(ns status-im.contexts.wallet.db
  (:require
    [status-im.contexts.wallet.db-path :as db-path]))

(def defaults
  {:ui {;; Note: we set it to nil by default to differentiate when the user logs
        ;; in and the device is offline, versus re-fetching when offline and
        ;; tokens already exist in the app-db.
        :tokens-loading nil
        :active-tab     :assets}})

(defn send
  [db]
  (get-in db db-path/send))
