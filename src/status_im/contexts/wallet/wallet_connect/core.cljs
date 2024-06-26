(ns status-im.contexts.wallet.wallet-connect.core
  (:require [clojure.edn :as edn]
            [clojure.string :as string]
            [native-module.core :as native-module]
            [status-im.constants :as constants]
            [utils.security.core :as security]
            [utils.transforms :as transforms]))

(def method-to-screen
  {constants/wallet-connect-personal-sign-method        :screen/wallet-connect.sign-message
   constants/wallet-connect-eth-sign-typed-method       :screen/wallet-connect.sign-message
   constants/wallet-connect-eth-sign-method             :screen/wallet-connect.sign-message
   constants/wallet-connect-eth-sign-typed-v4-method    :screen/wallet-connect.sign-message
   constants/wallet-connect-eth-send-transaction-method :screen/wallet-connect.send-transaction
   constants/wallet-connect-eth-sign-transaction-method :screen/wallet-connect.sign-transaction})

(defn extract-native-call-signature
  [data]
  (-> data transforms/json->clj :result))

(defn chain-id->eip155
  [chain-id]
  (str "eip155:" chain-id))

(defn eip155->chain-id
  [chain-id-str]
  (-> chain-id-str
      (string/split #":")
      last
      edn/read-string))

(defn format-eip155-address
  [address chain-id]
  (str chain-id ":" address))

(defn get-request-method
  [event]
  (get-in event [:params :request :method]))

(defn get-request-params
  [event]
  (get-in event [:params :request :params]))

(defn get-db-current-request-event
  [db]
  (get-in db [:wallet-connect/current-request :event]))

(defn get-db-current-request-params
  [db]
  (-> db
      get-db-current-request-event
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
