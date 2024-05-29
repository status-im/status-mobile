(ns status-im.contexts.wallet.wallet-connect.core
  (:require [native-module.core :as native-module]
            [utils.security.core :as security]
            [utils.transforms :as transforms]))

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

(def ^:private sign-typed-data-by-version
  {:v1 native-module/sign-typed-data
   :v4 native-module/sign-typed-data-v4})

(defn sign-typed-data
  [version data address password]
  (let [f (get sign-typed-data-by-version version)]
    (->> password
         security/safe-unmask-data
         (f data address))))
