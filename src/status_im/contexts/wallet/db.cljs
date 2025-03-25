(ns status-im.contexts.wallet.db)

(def defaults
  {:ui {;; Note: we set it to nil by default to differentiate when the user logs
        ;; in and the device is offline, versus re-fetching when offline and
        ;; tokens already exist in the app-db.
        :tokens-loading nil
        :active-tab     :assets}})
