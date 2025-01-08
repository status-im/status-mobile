(ns status-im.contexts.wallet.wallet-connect.utils.proposal
  (:require [clojure.string :as string]
            [schema.core :as schema]
            [status-im.contexts.wallet.wallet-connect.utils.dapp :as dapp-utils]
            [status-im.contexts.wallet.wallet-connect.utils.networks :as networks-utils]
            [utils.datetime :as datetime]))

(def ^:private ?proposer-metadata
  [:map
   [:name {:optional true} :string]
   [:url {:optional true} :string]
   [:description {:optional true} :string]
   [:icons {:optional true} [:sequential :string]]])

(def ^:private ?namespace
  [:map
   [:methods [:sequential :string]]
   [:chains [:sequential :string]]
   [:events [:sequential :string]]])

(def ^:private ?proposal
  [:map
   [:id :int]
   [:verifyContext
    [:map
     [:verified
      [:map
       [:validation :string]
       [:origin :string]
       [:verifyUrl :string]
       [:isScam {:optional true} :boolean]]]]]
   [:params
    [:map
     [:id :int]
     [:expiryTimestamp :int]
     [:pairingTopic :string]
     [:requiredNamespaces [:map-of :keyword ?namespace]]
     [:optionalNamespaces [:map-of :keyword ?namespace]]
     [:relays [:sequential :any]]
     [:proposer
      [:map
       [:publicKey :string]
       [:metadata ?proposer-metadata]]]]]])

(defn expired?
  [proposal]
  (some-> proposal
          :params
          :expiryTimestamp
          datetime/timestamp-expired?))

(schema/=> expired?
  [:=>
   [:cat ?proposal]
   :boolean])

(defn dapp-metadata
  [proposal]
  (let [metadata (get-in proposal [:params :proposer :metadata])
        origin   (get-in proposal [:verifyContext :verified :origin])]
    (or metadata {:url origin})))

(schema/=> dapp-metadata
  [:=>
   [:cat ?proposal]
   ?proposer-metadata])

(defn dapp-name
  [proposal]
  (let [{:keys [name url]} (-> proposal dapp-metadata)]
    (dapp-utils/compute-name name url)))

(schema/=> dapp-name
  [:=>
   [:cat ?proposal]
   [:maybe :string]])

(defn dapp-icon
  [proposal]
  (let [{:keys [icons url]} (-> proposal dapp-metadata)
        icon                (dapp-utils/best-icon icons)]
    (dapp-utils/compute-icon icon url)))

(schema/=> dapp-icon
  [:=>
   [:cat ?proposal]
   [:maybe :string]])

(defn dapp-url
  [proposal]
  (-> proposal dapp-metadata :url))

(schema/=> dapp-url
  [:=>
   [:cat ?proposal]
   [:maybe :string]])

(defn networks
  [proposal]
  (let [required-namespaces (get-in proposal [:params :requiredNamespaces])
        optional-namespaces (get-in proposal [:params :optionalNamespaces])]
    (->> [required-namespaces optional-namespaces]
         (map #(get-in % [:eip155 :chains]))
         (apply concat)
         (into #{}))))

(schema/=> networks
  [:=>
   [:cat ?proposal]
   [:set :string]])

(defn networks-intersection
  [proposal supported-networks]
  (let [proposed-networks (networks proposal)]
    (networks-utils/networks-intersection proposed-networks
                                          supported-networks)))

(schema/=> networks-intersection
  [:=>
   [:cat ?proposal [:sequential :int]]
   [:sequential :int]])

(defn required-networks-supported?
  [proposal supported-networks]
  (let [supported-namespaces #{:eip155}
        required-namespaces  (get-in proposal [:params :requiredNamespaces])
        required-networks    (get-in required-namespaces [:eip155 :chains])]
    (when (every? #(contains? supported-namespaces %)
                  (keys required-namespaces))
      (networks-utils/required-networks-supported? required-networks supported-networks))))

(schema/=> required-networks-supported?
  [:=>
   [:cat ?proposal [:sequential :int]]
   :boolean])
