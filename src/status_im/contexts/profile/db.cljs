(ns status-im.contexts.profile.db
  (:require [status-im.contexts.profile.core :as profile]))

(defn get-profile
  [db]
  (get db :profile/profile))

(defn testnet?
  [db]
  (-> db get-profile :test-networks-enabled? boolean))

(defn get-currency
  [db]
  (-> db get-profile profile/get-currency))
