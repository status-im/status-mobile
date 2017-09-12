(ns status-im.ui.screens.wallet.transactions.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [status-im.utils.datetime :as datetime]))

(reg-sub :wallet.transactions/transactions-loading?
  (fn [db]
    (get-in db [:wallet :transactions-loading?])))

(reg-sub :wallet.transactions/error-message?
  (fn [db]
    (get-in db [:wallet :errors :transactions-update])))

(reg-sub :wallet.transactions/transactions
  (fn [db]
    (group-by :type (get-in db [:wallet :transactions]))))

(reg-sub :wallet.transactions/unsigned-transactions
  :<- [:wallet.transactions/transactions]
  (fn [transactions]
    (:unsigned transactions)))

(reg-sub :wallet.transactions/postponed-transactions-list
  :<- [:wallet.transactions/transactions]
  (fn [{:keys [postponed]}]
    (when postponed
      {:title "Postponed"
       :key :postponed
       :data postponed})))

(reg-sub :wallet.transactions/pending-transactions-list
  :<- [:wallet.transactions/transactions]
  (fn [{:keys [pending]}]
    (when pending
      {:title "Pending"
       :key :pending
       :data pending})))

(reg-sub :wallet.transactions/completed-transactions-list
  :<- [:wallet.transactions/transactions]
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
