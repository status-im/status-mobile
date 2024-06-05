(ns status-im.subs.wallet.activities
  (:require
    [legacy.status-im.utils.hex :as utils.hex]
    [native-module.core :as native-module]
    [quo.foundations.resources :as quo.resources]
    [quo.foundations.resources]
    [re-frame.core :as rf]
    [status-im.contexts.wallet.common.activity-tab.constants :as constants]
    [utils.datetime :as datetime]
    [utils.money :as money]))

(def precision 6)

(rf/reg-sub
 :wallet/all-activities
 :<- [:wallet]
 :-> :activities)

(defn- activity-amount
  [amount]
  (-> amount
      (utils.hex/normalize-hex)
      (native-module/hex-to-number)
      (money/wei->ether)
      (money/with-precision precision)
      (str)))

(defn- process-send-activity
  [{:keys [symbol-out chain-id-out amount-out]} activity chain-id->network-name]
  (let [network-name (chain-id->network-name chain-id-out)]
    (assoc activity
           :transaction  :send
           :token        symbol-out
           :amount       (activity-amount amount-out)
           :network-name network-name
           :network-logo (quo.resources/get-network network-name))))

(defn- process-receive-activity
  [{:keys [symbol-in amount-in chain-id-in]} activity chain-id->network-name]
  (let [network-name (chain-id->network-name chain-id-in)]
    (assoc activity
           :transaction  :receive
           :token        symbol-in
           :amount       (activity-amount amount-in)
           :network-name network-name
           :network-logo (quo.resources/get-network network-name))))

(defn- process-activity-by-type
  [chain-id->network-name
   {:keys [activity-type activity-status timestamp sender recipient] :as data}]
  (let [activity {:relative-date (datetime/timestamp->relative (* timestamp 1000))
                  :timestamp     timestamp
                  :status        (constants/wallet-activity-status->name activity-status)
                  :sender        sender
                  :recipient     recipient}]
    (condp = activity-type
      constants/wallet-activity-type-send
      (process-send-activity data activity chain-id->network-name)

      constants/wallet-activity-type-receive
      (process-receive-activity data activity chain-id->network-name)

      nil)))

(rf/reg-sub
 :wallet/activities-for-current-viewing-account
 :<- [:wallet/all-activities]
 :<- [:wallet/current-viewing-account-address]
 :<- [:wallet/network-details]
 (fn [[activities current-viewing-account-address network-details]]
   (let [chain-id->network-name (update-vals (group-by :chain-id network-details)
                                             (comp :network-name first))]
     (->> current-viewing-account-address
          (get activities)
          (keep #(process-activity-by-type chain-id->network-name %))
          (group-by (fn [{:keys [timestamp]}]
                      (datetime/timestamp->relative-short-date (* timestamp 1000))))
          (map (fn [[date activities]]
                 {:title     date
                  :data      activities
                  :timestamp (:timestamp (first activities))}))
          (sort-by (fn [{:keys [timestamp]}] (- timestamp)))))))
