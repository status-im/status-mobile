(ns status-im.contexts.wallet.wallet-connect.utils.request
  (:require
    [schema.core :as schema]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.wallet-connect.utils.networks :as networks]
    [utils.datetime :as datetime]))

(def ^:private ?event
  [:map
   [:id :string]
   [:params
    [:map
     [:chainId :string]
     [:request
      [:map
       [:method :string]
       [:params :any]
       [:expiryTimestamp :int]]]]]
   [:verifyContext
    [:map
     [:verified
      [:map
       [:validation :string]
       [:origin :string]
       [:verifyUrl :string]
       [:isScam {:optional true} :boolean]]]]]])

(defn expired?
  [event]
  (some-> event
          :params
          :request
          :expiryTimestamp
          datetime/timestamp-expired?))

(schema/=> expired?
  [:=> [:cat ?event]
   [:maybe :boolean]])

(defn method
  [event]
  (get-in event [:params :request :method]))

(schema/=> method
  [:=> [:cat ?event]
   :string])

(defn chain
  [event]
  (-> event
      (get-in [:params :chainId])
      networks/eip155->chain-id))

(schema/=> chain
  [:=> [:cat ?event]
   :int])

(defn screen
  [event]
  (let [method-to-screen
        {constants/wallet-connect-personal-sign-method        :screen/wallet-connect.sign-message
         constants/wallet-connect-eth-sign-typed-method       :screen/wallet-connect.sign-message
         constants/wallet-connect-eth-sign-method             :screen/wallet-connect.sign-message
         constants/wallet-connect-eth-sign-typed-v4-method    :screen/wallet-connect.sign-message
         constants/wallet-connect-eth-send-transaction-method :screen/wallet-connect.send-transaction
         constants/wallet-connect-eth-sign-transaction-method :screen/wallet-connect.sign-transaction}]
    (-> event method method-to-screen)))

(schema/=> screen
  [:=> [:cat ?event]
   [:maybe :keyword]])

(defn params
  [event]
  (get-in event [:params :request :params]))

(schema/=> screen
  [:=> [:cat ?event]
   :any])

(defn url
  [event]
  (get-in event [:verifyContext :verified :origin]))

(schema/=> screen
  [:=> [:cat ?event]
   :string])
