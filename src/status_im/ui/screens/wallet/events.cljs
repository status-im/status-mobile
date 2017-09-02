(ns status-im.ui.screens.wallet.events
  (:require [re-frame.core :as re-frame :refer [dispatch reg-fx]]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.prices :as prices]
            [status-im.ui.screens.wallet.db :as wallet.db]
            [status-im.components.status :as status]
            [taoensso.timbre :as log]))

(defn get-balance [{:keys [web3 account-id on-success on-error]}]
  (if (and web3 account-id)
    (.getBalance
     (.-eth web3)
     account-id
     (fn [err resp]
       (if-not err
         (on-success resp)
         (on-error err))))
    (on-error "web3 or account-id not available")))

;; FX

(reg-fx
  :get-balance
  (fn [{:keys [web3 account-id success-event error-event]}]
    (get-balance
     {:web3           web3
      :account-id     account-id
      :on-success     #(dispatch [success-event %])
      :on-error       #(dispatch [error-event %])})))

(reg-fx
  :get-prices
  (fn [{:keys [from to success-event error-event]}]
    (prices/get-prices
     from
     to
     #(dispatch [success-event %])
     #(dispatch [error-event %]))))

;; Handlers

;; TODO(oskarth): At some point we want to get list of relevant assets to get prices for
(handlers/register-handler-fx
  :load-prices
  (fn [{{:keys [wallet] :as db} :db} [_ a]]
    {:get-prices  {:from          "ETH"
                   :to            "USD"
                   :success-event :update-prices
                   :error-event   :update-prices-fail}}))

(handlers/register-handler-fx
  :init-wallet
  (fn [{{:keys [web3 accounts/current-account-id] :as db} :db} [_ a]]
    {:get-balance {:web3          web3
                   :account-id    current-account-id
                   :success-event :update-balance
                   :error-event   :update-balance-fail}
     :dispatch    [:load-prices]
     :db (assoc-in db [:wallet :transactions] wallet.db/dummy-transaction-data)}))

(defn set-error-message [db err]
  (assoc-in db [:wallet :wallet/error] err))

(handlers/register-handler-db
  :wallet/clear-error-message
  (fn [db [_]]
    (update db :wallet dissoc :wallet/error)))

(handlers/register-handler-db
  :update-balance
  (fn [db [_ balance]]
    (assoc-in db [:wallet :balance] balance)))

(handlers/register-handler-db
  :update-prices
  (fn [db [_ prices]]
    (assoc db :prices prices)))

(handlers/register-handler-db
  :update-balance-fail
  (fn [db [_ err]]
    (log/debug "Unable to get balance: " err)
    (set-error-message db :error)))

(handlers/register-handler-db
  :update-prices-fail
  (fn [db [_ err]]
    (log/debug "Unable to get prices: " err)
    (set-error-message db :error)))
