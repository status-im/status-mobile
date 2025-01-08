(ns status-im.contexts.wallet.wallet-connect.utils.uri
  "Validating and parsing WalletConnect pairing URIs"
  (:require [malli.util :as malli.util]
            [react-native.wallet-connect :as wallet-connect]
            [schema.core :as schema]
            [utils.datetime :as datetime]))

(def ?wc-uri
  [:map
   [:protocol :string]
   [:topic :string]
   [:version :int]
   [:symKey [:maybe :string]]
   [:relay :map]
   [:methods {:optional true} [:maybe [:sequential :any]]]
   [:expiryTimestamp [:maybe :int]]])

(defn parse
  [text-uri]
  (wallet-connect/parse-uri text-uri))

(schema/=> parse
  [:=>
   [:cat :string]
   ?wc-uri])

(defn version-supported?
  [uri]
  (= 2 (:version uri)))

(schema/=> version-supported?
  [:=>
   [:cat ?wc-uri]
   :boolean])

(defn expired?
  [uri]
  (some-> uri
          :expiryTimestamp
          datetime/timestamp-expired?))

(schema/=> expired?
  [:=>
   [:cat ?wc-uri]
   [:maybe :boolean]])

(defn valid?
  [uri]
  (let [{:keys [topic version]} uri]
    (boolean (and (seq topic)
                  (number? version)))))

(schema/=> valid?
  [:=>
   [:cat (malli.util/optional-keys ?wc-uri)]
   [:maybe :boolean]])
