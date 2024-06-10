(ns status-im.subs.wallet.send
  (:require
    [re-frame.core :as rf]
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
 :<- [:wallet/activities-for-current-viewing-account]
 :<- [:wallet/current-viewing-account-address]
 (fn [[sections current-viewing-account-address]]
   (let [all-transactions        (mapcat :data sections)
         users-sent-transactions (filter (fn [{:keys [sender]}]
                                           (= sender current-viewing-account-address))
                                         all-transactions)]
     (set (map :recipient users-sent-transactions)))))

(rf/reg-sub
 :wallet/send-token-not-supported-in-receiver-networks?
 :<- [:wallet/wallet-send]
 :-> :token-not-supported-in-receiver-networks?)
