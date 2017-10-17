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
      (.sendTransaction (.-eth web3)
                        (clj->js (select-keys params [:from :to :value]))
                        #()))))

(re-frame/reg-fx
  ::show-transaction-moved
  (fn []
    (utils/show-popup (i18n/label :t/transaction-moved-title) (i18n/label :t/transaction-moved-text))))

;;;; Handlers

(handlers/register-handler-fx
  :wallet/set-and-validate-amount
  (fn [{:keys [db]} [_ amount]]
    (let [error (wallet.db/get-amount-validation-error amount)]
      {:db (-> db
               (assoc-in [:wallet :send-transaction :amount] amount)
               (assoc-in [:wallet :send-transaction :amount-error] error))})))

(def ^:private clear-send-properties {:id  nil
                                      :wrong-password? false
                                      :waiting-signal? false
                                      :from-chat? false})

(handlers/register-handler-fx
  ::transaction-completed
  (fn [{db :db} [_ {:keys [id response]}]]
    (let [{:keys [hash error]} response
          db' (assoc-in db [:wallet :send-transaction :in-progress?] false)]
      (if-not (and error (string? error) (not (string/blank? error)))
        {:db       (-> db'
                       (update-in [:wallet :transactions-unsigned] dissoc id)
                       (update-in [:wallet :send-transaction] merge clear-send-properties))
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
          db' (assoc-in db [:wallet :send-transaction :in-progress?] false)
          has-error? (and error (string? error) (not (string/blank? error)))]
      (if has-error?
        {:db db'}
        {:db       (-> db'
                       (update-in [:wallet :transactions-unsigned] dissoc id)
                       (update-in [:wallet :send-transaction] merge clear-send-properties))
         :dispatch-n [[:navigate-back]
                      [:navigate-to :wallet-transaction-sent]]}))))

(defn on-transactions-modal-completed [raw-results]
  (let [results (:results (types/json->clj raw-results))]
    (doseq [result results]
      (re-frame/dispatch [::transaction-modal-completed {:id (name (key result)) :response (second result)}]))))

(handlers/register-handler-fx
  :wallet.send-transaction/transaction-queued
  (fn [{:keys [db]} _]
    (let [{:keys [later? password id]} (get-in db [:wallet :send-transaction])]
      (if later?
        {:db                      (assoc-in db [:wallet :send-transaction :waiting-signal?] false)
         :dispatch                [:navigate-back]
         ::show-transaction-moved nil}
        {::accept-transaction {:id           id
                               :password     password
                               :on-completed on-transactions-completed}}))))

(handlers/register-handler-fx
  :wallet/sign-transaction
  (fn [{{:keys          [web3]
         :accounts/keys [accounts current-account-id] :as db} :db} [_ later?]]
    (let [{:keys [amount id password to-address]} (get-in db [:wallet :send-transaction])]
      (if id
        {::accept-transaction {:id           id
                               :password     password
                               :on-completed on-transactions-completed}
         :db (assoc-in db [:wallet :send-transaction :in-progress?] true)}
        {:db (update-in db [:wallet :send-transaction] assoc
                        :waiting-signal? true
                        :later? later?
                        :in-progress? true)
         ::send-transaction {:web3  web3
                             :from  (get-in accounts [current-account-id :address])
                             :to    to-address
                             :value (money/to-wei (money/normalize amount))}}))))

(handlers/register-handler-fx
  :wallet/sign-transaction-modal
  (fn [{{:keys          [web3]
         :accounts/keys [accounts current-account-id] :as db} :db} [_ later?]]
    (let [{:keys [id password]} (get-in db [:wallet :send-transaction])]
      {:db (assoc-in db [:wallet :send-transaction :in-progress?] true)
       ::accept-transaction {:id           id
                             :password     password
                             :on-completed on-transactions-modal-completed}})))

(defn discard-transaction
  [{:keys [db]}]
  (let [{:keys [id]} (get-in db [:wallet :send-transaction])]
    (merge {:db (update-in db [:wallet :send-transaction] merge clear-send-properties)}
           (when id
             {:discard-transaction id}))))

(handlers/register-handler-fx
  :wallet/discard-transaction
  (fn [cofx _]
    (discard-transaction cofx)))

(handlers/register-handler-fx
  :wallet/discard-transaction-navigate-back
  (fn [cofx _]
    (-> cofx
        discard-transaction
        (assoc :dispatch [:navigate-back]))))

(handlers/register-handler-fx
  :wallet/cancel-signing-modal
  (fn [{:keys [db]} _]
    {:db (update-in db [:wallet :send-transaction] assoc
                    :signing? false
                    :wrong-password? false)}))

(handlers/register-handler-fx
  :wallet.send/set-camera-dimensions
  (fn [{:keys [db]} [_ camera-dimensions]]
    {:db (assoc-in db [:wallet :send-transaction :camera-dimensions] camera-dimensions)}))

(handlers/register-handler-fx
  :wallet.send/set-password
  (fn [{:keys [db]} [_ password]]
    {:db (assoc-in db [:wallet :send-transaction :password] password)}))

(handlers/register-handler-fx
  :wallet.send/set-signing?
  (fn [{:keys [db]} [_ signing?]]
    {:db (assoc-in db [:wallet :send-transaction :signing?] signing?)}))
