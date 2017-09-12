(ns status-im.ui.screens.wallet.transactions.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.money :as money]
            [status-im.utils.transactions :as transactions]))

(reg-sub :wallet.transactions/transactions-loading?
  :<- [:wallet]
  (fn [wallet]
    (:transactions-loading? wallet)))

(reg-sub :wallet.transactions/error-message?
  :<- [:wallet]
  (fn [wallet]
    (get-in wallet [:errors :transactions-update])))

(reg-sub :wallet.transactions/transactions
  :<- [:wallet]
  (fn [wallet]
    (:transactions wallet)))

(reg-sub :wallet.transactions/grouped-transactions
  :<- [:wallet.transactions/transactions]
  (fn [transactions]
    (group-by :type (vals transactions))))

(reg-sub :wallet.transactions/postponed-transactions-list
  :<- [:wallet.transactions/grouped-transactions]
  (fn [{:keys [postponed]}]
    (when postponed
      {:title "Postponed"
       :key :postponed
       :data postponed})))

(reg-sub :wallet.transactions/pending-transactions-list
  :<- [:wallet.transactions/grouped-transactions]
  (fn [{:keys [pending]}]
    (when pending
      {:title "Pending"
       :key :pending
       :data pending})))

(reg-sub :wallet.transactions/completed-transactions-list
  :<- [:wallet.transactions/grouped-transactions]
  (fn [{:keys [inbound outbound]}]
    (->> (into inbound outbound)
         (group-by #(datetime/timestamp->date-key (:timestamp %)))
         (sort-by key)
         reverse
         (map (fn [[k v]]
                {:title (datetime/timestamp->mini-date (:timestamp (first v)))
                 :key   k
                 ;; TODO (yenda investigate wether this sort-by is necessary or not)
                 :data  (sort-by :timestamp v)})))))

(reg-sub :wallet.transactions/transactions-history-list
  :<- [:wallet.transactions/postponed-transactions-list]
  :<- [:wallet.transactions/pending-transactions-list]
  :<- [:wallet.transactions/completed-transactions-list]
  (fn [[postponed pending completed]]
    (cond-> []
      postponed (into postponed)
      pending   (into pending)
      completed (into completed))))

(reg-sub :wallet.transactions/current-transaction
  :<- [:wallet]
  (fn [wallet]
    (:current-transaction wallet)))

(reg-sub :wallet.transactions/transaction-details
  :<- [:wallet.transactions/transactions]
  :<- [:wallet.transactions/current-transaction]
  :<- [:network]
  (fn [[transactions current-transaction network]]
    (let [{:keys [gas-used gas-price hash timestamp type] :as transaction} (get transactions current-transaction)]
      (merge transaction
             {:cost (money/wei->ether (money/fee-value gas-used gas-price))
              :gas-price-eth  (str (.toFixed (money/wei->ether gas-price)) " ETH")
              :date (datetime/timestamp->long-date timestamp)
              :url (transactions/get-transaction-details-url network hash)}
             ;; TODO (yenda) proper wallet logic when wallet switching is impletmented
             (if (= type :inbound)
               {:to-wallet "Main wallet"}
               {:from-wallet "Main wallet"})))))

(reg-sub :wallet.transactions.details/confirmations
  :<- [:wallet.transactions/transaction-details]
  (fn [transaction-details]
    ;;TODO (yenda) this field should be calculated based on the current-block and the block of the transaction
    (:confirmations transaction-details)))

(reg-sub :wallet.transactions.details/confirmations-progress
  :<- [:wallet.transactions.details/confirmations]
  (fn [confirmations]
    (let [max-confirmations 10]
      (if (>= confirmations max-confirmations)
        100
        (* 100 (/ confirmations max-confirmations))))))
