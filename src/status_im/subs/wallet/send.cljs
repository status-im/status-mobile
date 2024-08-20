(ns status-im.subs.wallet.send
  (:require
    [re-frame.core :as rf]
    [status-im.contexts.wallet.common.activity-tab.constants :as constants]
    [utils.number]))

(rf/reg-sub
 :wallet/send-tab
 :<- [:wallet/ui]
 (fn [ui]
   (get-in ui [:send :select-address-tab])))

(rf/reg-sub
 :wallet/wallet-send
 :<- [:wallet/ui]
 :-> :send)

(rf/reg-sub
 :wallet/wallet-send-recipient
 :<- [:wallet/wallet-send]
 :-> :recipient)

(rf/reg-sub
 :wallet/send-transaction-ids
 :<- [:wallet/wallet-send]
 :-> :transaction-ids)

(rf/reg-sub
 :wallet/just-completed-transaction
 :<- [:wallet/wallet-send]
 :-> :just-completed-transaction?)

(rf/reg-sub
 :wallet/wallet-send-amount
 :<- [:wallet/wallet-send]
 :-> :amount)

(rf/reg-sub
 :wallet/send-transaction-progress
 :<- [:wallet/send-transaction-ids]
 :<- [:wallet/transactions]
 (fn [[tx-ids transactions]]
   (let [send-tx-ids (set (keys transactions))]
     (select-keys transactions
                  (filter send-tx-ids tx-ids)))))

(rf/reg-sub
 :wallet/recent-recipients
 :<- [:wallet/all-activities]
 :<- [:wallet/current-viewing-account-address]
 (fn [[all-activities current-viewing-account-address]]
   (let [address-activity (vals (get all-activities current-viewing-account-address))]
     (->> address-activity
          (sort :timestamp)
          (keep (fn [{:keys [activity-type recipient]}]
                  (when (= constants/wallet-activity-type-send activity-type)
                    recipient)))
          (distinct)))))

(rf/reg-sub
 :wallet/send-token-not-supported-in-receiver-networks?
 :<- [:wallet/wallet-send]
 :-> :token-not-supported-in-receiver-networks?)

(rf/reg-sub
 :wallet/bridge-from-networks
 :<- [:wallet/wallet-send]
 :<- [:wallet/network-details]
 (fn [[{:keys [bridge-to-chain-id]} networks]]
   (set (filter (fn [network]
                  (not= (:chain-id network) bridge-to-chain-id))
                networks))))

(rf/reg-sub
 :wallet/bridge-from-chain-ids
 :<- [:wallet/wallet-send]
 :<- [:wallet/networks-by-mode]
 (fn [[{:keys [bridge-to-chain-id]} networks]]
   (keep (fn [network]
           (when (not= (:chain-id network) bridge-to-chain-id)
             (:chain-id network)))
         networks)))
