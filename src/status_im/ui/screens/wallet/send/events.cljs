(ns status-im.ui.screens.wallet.send.events
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.ui.screens.wallet.db :as wallet.db]
            [status-im.native-module.core :as status]
            [status-im.utils.types :as types]
            [clojure.string :as string]
            [status-im.utils.money :as money]
            [status-im.utils.utils :as utils]
            [status-im.i18n :as i18n]))

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

(re-frame/reg-fx
  ::discard-transaction
  (fn [id]
    (status/discard-transaction id)))

;;;; Handlers

(defn choose-address-and-name [db address name]
  (update db :wallet/send-transaction assoc :to-address address :to-name name))

(handlers/register-handler-fx
  :choose-recipient
  (fn [{:keys [db]} [_ address name]]
    (let [{:keys [view-id]} db]
      (cond-> {:db (choose-address-and-name db address name)}
        (= :choose-recipient view-id) (assoc :dispatch [:navigate-back])))))

(handlers/register-handler-fx
  :wallet-open-send-transaction
  (fn [{db :db} [_ address name]]
    {:db         (choose-address-and-name db address name)
     :dispatch-n [[:navigate-back]
                  [:navigate-back]]}))

(handlers/register-handler-fx
  :wallet-validate-amount
  (fn [{{:keys [web3] :wallet/keys [send-transaction] :as db} :db} _]
    (let [amount (:amount send-transaction)
          error (wallet.db/get-amount-validation-error amount web3)]
      {:db (assoc-in db [:wallet/send-transaction :amount-error] error)})))

(handlers/register-handler-fx
  ::transaction-completed
  (fn [{db :db} [_ {:keys [id response]}]]
    (let [{:keys [hash error]} response]
      (when-not (and error (string? error) (not (string/blank? error)))
        {:db       (-> db
                       (assoc-in [:wallet/send-transaction :transaction-id] nil)
                       (assoc :wrong-password? false))
         :dispatch [:navigate-to :wallet-transaction-sent]}))))

(defn on-transactions-completed [raw-results]
  (let [results (:results (types/json->clj raw-results))]
    (doseq [result results]
      ;;TODO (andrey) legacy, should be removed with old transactions screens
      (re-frame/dispatch [:transaction-completed {:id (name (key result)) :response (second result)}])
      (re-frame/dispatch [::transaction-completed {:id (name (key result)) :response (second result)}]))))

(handlers/register-handler-fx
  :wallet/transaction-queued
  (fn [{{:wallet/keys [send-transaction] :as db} :db} [_ transaction-id]]
    (let [{:keys [later? password]} send-transaction]
      (if later?
        {:db                      (-> db
                                      (assoc-in [:wallet/send-transaction :waiting-signal?] false)
                                      (assoc :transactions (:transactions-queue db)))
         :dispatch                [:navigate-back]
         ::show-transaction-moved nil}
        {:db                  (assoc-in db [:wallet/send-transaction :transaction-id] transaction-id)
         ::accept-transaction {:id           transaction-id
                               :password     password
                               :on-completed on-transactions-completed}}))))

(handlers/register-handler-fx
  :wallet/sign-transaction
  (fn [{{:keys          [web3]
         :wallet/keys   [send-transaction]
         :accounts/keys [accounts current-account-id] :as db} :db} [_ later?]]
    (let [{:keys [amount transaction-id password]} send-transaction
          amount-in-wei (money/to-wei (string/replace amount #"," "."))]
      (if transaction-id
        {::accept-transaction {:id           transaction-id
                               :password     password
                               :on-completed on-transactions-completed}}
        {:db                (update-in db [:wallet/send-transaction]
                                       #(assoc % :waiting-signal? true
                                                 :later? later?))
         ::send-transaction {:web3  web3
                             :from  (get-in accounts [current-account-id :address])
                             :to    (:to-address send-transaction)
                             :value amount-in-wei}}))))

(handlers/register-handler-fx
  :wallet/discard-transaction
  (fn [{{:wallet/keys   [send-transaction] :as db} :db} _]
    (let [{:keys [transaction-id]} send-transaction]
      (merge {:db (-> db
                      (update-in [:wallet/send-transaction]
                                 #(assoc % :signing? false :transaction-id nil))
                      (assoc :wrong-password? false))}
             (when transaction-id
               ;;TODO (andrey) use ::discard-transaction fx instead
               {:dispatch-n [[:deny-transaction transaction-id]
                             [:status-im.transactions.handlers/remove-transaction transaction-id]]})))))

(handlers/register-handler-db
  :wallet/toggle-flashlight
  (fn [db]
    (let [flashlight-state (get-in db [:wallet/send-transaction :camera-flashlight])
          toggled-state (if (= :on flashlight-state) :off :on)]
      (assoc-in db [:wallet/send-transaction :camera-flashlight] toggled-state))))
