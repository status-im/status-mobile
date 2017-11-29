(ns status-im.ui.screens.wallet.send.events
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.native-module.core :as status]
            [status-im.utils.handlers :as handlers]
            [status-im.ui.screens.wallet.db :as wallet.db]
            [status-im.utils.types :as types]
            [status-im.utils.money :as money]
            [status-im.utils.utils :as utils]
            [status-im.utils.hex :as utils.hex]
            [status-im.constants :as constants]))

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

(re-frame/reg-fx
  ::show-transaction-error
  (fn [message]
    ;; (andrey) we need this timeout because modal window conflicts with alert
    (js/setTimeout #(utils/show-popup (i18n/label :t/transaction-failed) message) 1000)))

(re-frame/reg-fx
  :discard-transaction
  (fn [id]
    (status/discard-transaction id)))

;;Helper functions

(defn transaction-valid? [{{:keys [to data]} :args}]
  (or (and to (utils.hex/valid-hex? to)) (and data (not= data "0x"))))

(defn dispatch-transaction-completed [result & [modal?]]
  (re-frame/dispatch [::transaction-completed {:id (name (key result)) :response (second result)} modal?]))
;;;; Handlers

(handlers/register-handler-fx
  :wallet.send/set-and-validate-amount
  (fn [{:keys [db]} [_ amount]]
    (let [{:keys [value error]} (wallet.db/parse-amount amount)]
      {:db (-> db
               (assoc-in [:wallet :send-transaction :amount] (money/ether->wei value))
               (assoc-in [:wallet :send-transaction :amount-error] error))})))

(def ^:private clear-send-properties {:id              nil
                                      :signing?        false
                                      :wrong-password? false
                                      :waiting-signal? false
                                      :from-chat?      false})

(defn on-transactions-completed [raw-results]
  (let [results (:results (types/json->clj raw-results))]
    (doseq [result results]
      (dispatch-transaction-completed result))))

;;TRANSACTION QUEUED signal from status-go
(handlers/register-handler-fx
  :transaction-queued
  [(re-frame/inject-cofx :now)]
  (fn [{:keys [db now]} [_ {:keys [id message_id args] :as transaction}]]
    (if (transaction-valid? transaction)
      (let [{:keys [from to value data gas gasPrice]} args
            ;;TODO (andrey) revisit this map later (this map from old transactions, idk if we need all these fields)
            transaction {:id         id
                         :from       from
                         :to         to
                         :value      (money/bignumber (or value 0))
                         :data       data
                         :gas        (money/to-decimal gas)
                         :gas-price  (money/to-decimal gasPrice)
                         :timestamp  now
                         :message-id message_id}
            sending-from-chat? (not (get-in db [:wallet :send-transaction :waiting-signal?]))
            new-db (assoc-in db [:wallet :transactions-unsigned id] transaction)
            sending-db {:id         id
                        :from-chat? sending-from-chat?}]
        (if sending-from-chat?
          ;;SENDING FROM CHAT
          {:db       (assoc-in new-db [:wallet :send-transaction] sending-db) ; we need to completely reset sending state here
           :dispatch [:navigate-to-modal :wallet-send-transaction-modal]}
          ;;SEND SCREEN WAITING SIGNAL
          (let [{:keys [later? password]} (get-in db [:wallet :send-transaction])
                new-db' (update-in new-db [:wallet :send-transaction] merge sending-db)] ; just update sending state as we are in wallet flow
            (if later?
              ;;SIGN LATER
              {:db                      (assoc-in new-db' [:wallet :send-transaction :waiting-signal?] false)
               :dispatch                [:navigate-back]
               ::show-transaction-moved nil}
              ;;SIGN NOW
              {:db                  new-db'
               ::accept-transaction {:id           id
                                     :password     password
                                     :on-completed on-transactions-completed}}))))
      {:discard-transaction id})))

(defn this-transaction-signing? [id signing-id view-id modal]
  (and (= signing-id id)
       (or (= view-id :wallet-send-transaction)
           (= modal :wallet-send-transaction-modal))))

;;TRANSACTION FAILED signal from status-go
(handlers/register-handler-fx
  :transaction-failed
  (fn [{{:keys [view-id modal] :as db} :db} [_ {:keys [id error_code error_message] :as event}]]
    (let [send-transaction (get-in db [:wallet :send-transaction])]
      (case error_code

        ;;WRONG PASSWORD
        constants/send-transaction-password-error-code
        {:db (assoc-in db [:wallet :send-transaction :wrong-password?] true)}

        ;;NO ERROR, DISCARDED, TIMEOUT or DEFAULT ERROR
        (if (this-transaction-signing? id (:id send-transaction) view-id modal)
          {:db                      (-> db
                                        (update-in [:wallet :transactions-unsigned] dissoc id)
                                        (update-in [:wallet :send-transaction] merge clear-send-properties))
           :dispatch                [:navigate-back]
           ::show-transaction-error error_message}
          {:db (update-in db [:wallet :transactions-unsigned] dissoc id)})))))

(handlers/register-handler-fx
  ::transaction-completed
  (fn [{db :db} [_ {:keys [id response]} modal?]]
    (let [{:keys [hash error]} response
          db' (assoc-in db [:wallet :send-transaction :in-progress?] false)]
      (if (and error (string? error) (not (string/blank? error))) ;; ignore error here, error will be handled in :transaction-failed
        {:db db'}
        (merge
          {:db (-> db'
                   (update-in [:wallet :transactions-unsigned] dissoc id)
                   (update-in [:wallet :send-transaction] merge clear-send-properties))}
          (if modal?
            {:dispatch-n [[:navigate-back]
                          [:navigate-to-modal :wallet-transaction-sent-modal]]}
            {:dispatch [:navigate-to :wallet-transaction-sent]}))))))

(defn on-transactions-modal-completed [raw-results]
  (let [results (:results (types/json->clj raw-results))]
    (doseq [result results]
      (dispatch-transaction-completed result true))))

(handlers/register-handler-fx
  :wallet/sign-transaction
  (fn [{{:keys          [web3]
         :accounts/keys [accounts current-account-id] :as db} :db} [_ later?]]
    (let [db' (assoc-in db [:wallet :send-transaction :wrong-password?] false)
          {:keys [amount id password to-address]} (get-in db [:wallet :send-transaction])]
      (if id
        {::accept-transaction {:id           id
                               :password     password
                               :on-completed on-transactions-completed}
         :db                  (assoc-in db' [:wallet :send-transaction :in-progress?] true)}
        {:db                (update-in db' [:wallet :send-transaction] assoc
                                       :waiting-signal? true
                                       :later? later?
                                       :in-progress? true)
         ::send-transaction {:web3  web3
                             :from  (get-in accounts [current-account-id :address])
                             :to    to-address
                             :value amount}}))))

(handlers/register-handler-fx
  :wallet/sign-transaction-modal
  (fn [{{:keys          [web3]
         :accounts/keys [accounts current-account-id] :as db} :db} [_ later?]]
    (let [{:keys [id password]} (get-in db [:wallet :send-transaction])]
      {:db                  (assoc-in db [:wallet :send-transaction :in-progress?] true)
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
