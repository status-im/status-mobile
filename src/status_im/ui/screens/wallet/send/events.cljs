(ns status-im.ui.screens.wallet.send.events
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.native-module.core :as status]
            [status-im.utils.handlers :as handlers]
            [status-im.ui.screens.wallet.db :as wallet.db]
            [status-im.utils.types :as types]
            [status-im.utils.money :as money]
            [status-im.utils.utils :as utils]))

;;;; FX

(re-frame/reg-fx
  ::accept-transaction
  (fn [{:keys [password id on-completed]}]
    (status/complete-transactions (list id) password on-completed)))

(re-frame/reg-fx
  ::send-transaction
  (fn [{:keys [web3] :as params}]
    (when web3
      (.sendTransaction
        (.-eth web3)
        (clj->js (select-keys params [:from :to :value]))
        #()))))

(re-frame/reg-fx
  ::show-transaction-moved
  (fn []
    (utils/show-popup (i18n/label :t/transaction-moved-title) (i18n/label :t/transaction-moved-text))))

;;;; Handlers

(handlers/register-handler-fx
  :wallet/set-and-validate-amount
  (fn [{{:wallet/keys [send-transaction] :as db} :db} [_ amount]]
    (let [error (wallet.db/get-amount-validation-error amount)]
      {:db (-> db
               (assoc-in [:wallet/send-transaction :amount] amount)
               (assoc-in [:wallet/send-transaction :amount-error] error))})))

(handlers/register-handler-fx
  ::transaction-completed
  (fn [{db :db} [_ {:keys [id response]}]]
    (let [{:keys [hash error]} response
          db' (assoc-in db [:wallet/send-transaction :in-progress?] false)]
      (if-not (and error (string? error) (not (string/blank? error)))
        {:db       (-> db'
                       (update-in [:wallet :transactions-unsigned] dissoc id)
                       (assoc-in [:wallet/send-transaction :transaction-id] nil)
                       (assoc-in [:wallet/send-transaction :wrong-password?] false))
         :dispatch [:navigate-to :wallet-transaction-sent]}
        {:db db'}))))

(defn on-transactions-completed [raw-results]
  (let [results (:results (types/json->clj raw-results))]
    (doseq [result results]
      (re-frame/dispatch [::transaction-completed {:id (name (key result)) :response (second result)}]))))

(handlers/register-handler-fx
  ::transaction-modal-completed
  (fn [{db :db} [_ {:keys [id response]}]]
    (let [{:keys [hash error]} response
          db' (assoc-in db [:wallet/send-transaction :in-progress?] false)
          has-error? (and error (string? error) (not (string/blank? error)))]
      (if has-error?
        {:db db'}
        {:db       (-> db'
                       (update-in [:wallet :transactions-unsigned] dissoc id)
                       (update :wallet/send-transaction assoc
                               :transaction-id nil
                               :wrong-password? false))
         :dispatch [:navigate-back]}))))

(defn on-transactions-modal-completed [raw-results]
  (let [results (:results (types/json->clj raw-results))]
    (doseq [result results]
      (re-frame/dispatch [::transaction-modal-completed {:id (name (key result)) :response (second result)}]))))

(handlers/register-handler-fx
  :wallet.send-transaction/transaction-queued
  (fn [{{:wallet/keys [send-transaction] :as db} :db} _]
    (let [{:keys [later? password transaction-id]} send-transaction]
      (if later?
        {:db                      (assoc-in db [:wallet/send-transaction :waiting-signal?] false)
         :dispatch                [:navigate-back]
         ::show-transaction-moved nil}
        {::accept-transaction {:id           transaction-id
                               :password     password
                               :on-completed on-transactions-completed}}))))

(handlers/register-handler-fx
  :wallet/sign-transaction
  (fn [{{:keys          [web3]
         :wallet/keys   [send-transaction]
         :accounts/keys [accounts current-account-id] :as db} :db} [_ later?]]
    (let [{:keys [amount transaction-id password]} send-transaction]
      (if transaction-id
        {::accept-transaction {:id           transaction-id
                               :password     password
                               :on-completed on-transactions-completed}
         :db (assoc-in db [:wallet/send-transaction :in-progress?] true)}
        {:db (update db :wallet/send-transaction assoc
                     :waiting-signal? true
                     :later? later?
                     :in-progress? true)
         ::send-transaction {:web3  web3
                             :from  (get-in accounts [current-account-id :address])
                             :to    (:to-address send-transaction)
                             :value (money/to-wei (money/normalize amount))}}))))

(handlers/register-handler-fx
  :wallet/sign-transaction-modal
  (fn [{{:keys          [web3]
         :wallet/keys   [send-transaction]
         :accounts/keys [accounts current-account-id] :as db} :db} [_ later?]]
    (let [{:keys [transaction-id password]} send-transaction]
      {:db (assoc-in db [:wallet/send-transaction :in-progress?] true)
       ::accept-transaction {:id           transaction-id
                             :password     password
                             :on-completed on-transactions-modal-completed}})))

(handlers/register-handler-fx
  :wallet/discard-transaction
  (fn [{{:wallet/keys   [send-transaction] :as db} :db} _]
    (let [{:keys [transaction-id]} send-transaction]
      (merge {:db (update db :wallet/send-transaction assoc
                          :signing? false
                          :transaction-id nil
                          :wrong-password? false)}
             (when transaction-id
               {:discard-transaction transaction-id})))))

(handlers/register-handler-fx
  :wallet/cancel-signing-modal
  (fn [{{:wallet/keys   [send-transaction] :as db} :db} _]
    (let [{:keys [transaction-id]} send-transaction]
      {:db (update-in db [:wallet/send-transaction]
                      assoc :signing? false :wrong-password? false)})))
