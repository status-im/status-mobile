(ns status-im.ui.screens.wallet.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [clojure.string :as string]
            [status-im.utils.money :as money]
            [status-im.utils.datetime :as datetime]))

(reg-sub :balance
  (fn [db]
    (get-in db [:wallet :balance])))

(reg-sub :price
  (fn [db]
    (get-in db [:prices :price])))

(reg-sub :last-day
  (fn [db]
    (get-in db [:prices :last-day])))

(reg-sub :wallet/error-message?
  (fn [db]
    (or (get-in db [:wallet :errors :balance-update])
        (get-in db [:wallet :errors :prices-update]))))

(reg-sub :wallet.transactions/error-message?
  (fn [db]
    (get-in db [:wallet :errors :transactions-update])))

(reg-sub :eth-balance
  :<- [:balance]
  (fn [balance]
    (if balance
      (money/wei->ether balance)
      "...")))

(reg-sub :portfolio-value
  :<- [:balance]
  :<- [:price]
  (fn [[balance price]]
    (if (and balance price)
      (-> (money/wei->ether balance)
          (money/eth->usd price)
          (money/with-precision 2)
          str)
      "...")))

(reg-sub :portfolio-change
  :<- [:price]
  :<- [:last-day]
  (fn [[price last-day]]
    (if (and price last-day)
      (-> (money/percent-change price last-day)
          (money/with-precision 2)
          (str "%"))
      "-%")))

(reg-sub :prices-loading?
  (fn [db]
    (:prices-loading? db)))

(reg-sub :wallet/balance-loading?
  (fn [db]
    (get-in db [:wallet :balance-loading?])))

(reg-sub :wallet/transactions-loading?
  (fn [db]
    (get-in db [:wallet :transactions-loading?])))

(reg-sub :wallet/transactions
  (fn [db]
    (get-in db [:wallet :transactions])))

(defn filter-transactions [type transactions]
  (filter #(= (:type %) type) transactions))

(reg-sub :wallet/unsigned-transactions
  :<- [:wallet/transactions]
  (fn [transactions]
    (filter-transactions :unsigned transactions)))

(defn mini-str-date->keyword [mini-str-date]
  (keyword (str "sent-" (string/replace mini-str-date #" " "-"))))

(reg-sub :wallet/transactions-history-list
  :<- [:wallet/transactions]
  (fn [transactions]
    (let [{:keys [postponed pending inbound outbound]} (group-by :type transactions)
          transaction-history-list                     [{:title "Postponed"
                                                         :key :postponed
                                                         :data (or postponed [])}
                                                        {:title "Pending"
                                                         :key :pending
                                                         :data (or pending [])}]
          completed-transactions                       (->> (into inbound outbound)
                                                            (group-by #(datetime/date->mini-str-date (:timestamp %)))
                                                            (map (fn [[k v]] {:title k
                                                                              :key (mini-str-date->keyword k)
                                                                              :data v})))]
      (into transaction-history-list (or completed-transactions [])))))
