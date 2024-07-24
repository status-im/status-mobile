(ns status-im.subs.wallet.activities
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [quo.foundations.resources :as quo.resources]
    [quo.foundations.resources]
    [re-frame.core :as rf]
    [status-im.contexts.wallet.common.activity-tab.constants :as constants]
    [utils.datetime :as datetime]
    [utils.hex :as utils.hex]
    [utils.money :as money]))

(def precision 6)

(rf/reg-sub
 :wallet/all-activities
 :<- [:wallet]
 :-> :activities)

(defn- hex-wei->amount
  [hex-str-amount]
  (-> hex-str-amount
      (utils.hex/normalize-hex)
      (native-module/hex-to-number)
      (money/wei->ether)
      (money/with-precision precision)
      (str)))

(defn- hex-string->number
  [hex-str-amount]
  (-> hex-str-amount
      (utils.hex/normalize-hex)
      (native-module/hex-to-number)
      (str)))

(defn- normalize-nft-name
  [token-id nft-name]
  (if (and (some? token-id) (string/blank? nft-name))
    "Unknown"
    nft-name))

(defn- get-token-amount
  [token amount]
  (let [token-type (:token-type token)]
    (if (#{constants/wallet-activity-token-type-erc-721
           constants/wallet-activity-token-type-erc-1155}
         token-type)
      (hex-string->number amount)
      (hex-wei->amount amount))))

(defn- process-send-activity
  [{:keys [symbol-out amount-out token-out]
    :as   data}]
  (assoc data
         :transaction :send
         :token       symbol-out
         :amount      (get-token-amount token-out amount-out)))

(defn- process-receive-activity
  [{:keys [symbol-in amount-in token-in] :as data}]
  (assoc data
         :transaction :receive
         :token       symbol-in
         :amount      (get-token-amount token-in amount-in)))

;; WIP to add the mint activity.
;(defn- process-mint-activity
;  [{:keys [token-in symbol-in amount-in chain-id-in nft-name] :as data}
;   chain-id->network-name]
;  (-> data
;      (merge activity)
;      (assoc :transaction :mint
;             ;:token        symbol-in
;             ;:amount       (activity-amount amount-in)
;             :nft-name (normalize-nft-name token-id nft-name))))

(defn- process-activity-by-type
  [chain-id->network-name
   {:keys [activity-type activity-status timestamp sender recipient token-in token-out
           chain-id-in chain-id-out nft-name]
    :as   data}]
  (let [network-name (chain-id->network-name (or chain-id-in chain-id-out))
        token-id     (some-> (or token-in token-out)
                             :token-id
                             hex-string->number)
        activity     (assoc data
                            :relative-date (datetime/timestamp->relative (* timestamp 1000))
                            :sender        sender
                            :recipient     recipient
                            :timestamp     timestamp
                            :network-name  network-name
                            :token-id      token-id
                            :status        (constants/wallet-activity-status->name activity-status)
                            :network-logo  (quo.resources/get-network network-name)
                            :nft-name      (normalize-nft-name token-id nft-name))]
    (condp = activity-type
      constants/wallet-activity-type-send
      (process-send-activity activity)

      constants/wallet-activity-type-receive
      (process-receive-activity activity)

      ;; WIP to add the mint activity. Constants/wallet-activity-type-mint
      ;; (process-mint-activity activity chain-id->network-name)

      nil)))

(rf/reg-sub
 :wallet/activities-for-current-viewing-account
 :<- [:wallet/all-activities]
 :<- [:wallet/current-viewing-account-address]
 :<- [:wallet/network-details]
 (fn [[activities current-viewing-account-address network-details]]
   (let [chain-id->network-name (update-vals (group-by :chain-id network-details)
                                             (comp :network-name first))
         address-activities     (->> (get activities current-viewing-account-address)
                                     (vals)
                                     (sort :timestamp))]
     (->> address-activities
          (keep #(process-activity-by-type chain-id->network-name %))
          (group-by (fn [{:keys [timestamp]}]
                      (datetime/timestamp->relative-short-date (* timestamp 1000))))
          (map (fn [[date activities]]
                 {:title     date
                  :data      activities
                  :timestamp (:timestamp (first activities))}))
          (sort-by (fn [{:keys [timestamp]}] (- timestamp)))))))
