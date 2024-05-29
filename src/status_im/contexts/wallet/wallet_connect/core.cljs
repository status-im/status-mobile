(ns status-im.contexts.wallet.wallet-connect.core
  (:require [utils.transforms :as transforms]))

(defn extract-native-call-signature
  [data]
  (-> data transforms/json->clj :result))

(defn chain-id->eip155
  [chain-id]
  (str "eip155:" chain-id))

(defn format-eip155-address
  [address chain-id]
  (str chain-id ":" address))

(defn get-request-method
  [event]
  (get-in event [:params :request :method]))

(defn get-request-params
  [event]
  (get-in event [:params :request :params]))

(defn get-db-current-request-params
  [db]
  (-> (get-in db [:wallet-connect/current-request :event])
      get-request-params))
