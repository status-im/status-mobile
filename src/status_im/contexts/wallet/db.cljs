(ns status-im.contexts.wallet.db
  (:require [status-im.constants :as constants]))

(def network-filter-defaults
  {:selector-state    :default
   :selected-networks (set constants/default-network-names)})

(def defaults
  {:ui {:network-filter network-filter-defaults
        ;; Note: we set it to nil by default to differentiate when the user logs
        ;; in and the device is offline, versus re-fetching when offline and
        ;; tokens already exist in the app-db.
        :tokens-loading nil}})
