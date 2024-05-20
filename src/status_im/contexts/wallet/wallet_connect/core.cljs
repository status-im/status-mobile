(ns status-im.contexts.wallet.wallet-connect.core
  (:require [status-im.constants :as constants]
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

(defn event->method
  [event]
  (get-in event [:params :request :method]))

(defn method->screen
  [method]
  (-> {constants/wallet-connect-personal-sign-method     :screen/wallet-connect.sign-message
       constants/wallet-connect-eth-sign-typed-method    :screen/wallet-connect.sign-message
       constants/wallet-connect-eth-sign-method          :screen/wallet-connect.sign-message
       constants/wallet-connect-eth-sign-typed-v4-method :screen/wallet-connect.sign-message}
      (get method)))
