(ns status-im.ui.screens.wallet.transactions.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.constants :as constants]
            [status-im.utils.money :as money]
            [status-im.native-module.core :as status]
            [re-frame.core :as re-frame]
            [status-im.utils.hex :as utils.hex]
            [clojure.string :as string]))

;;FX

(re-frame/reg-fx
  :discard-transaction
  (fn [id]
    (status/discard-transaction id)))

;;Helper functions

(defn transaction-valid? [{{:keys [to data]} :args}]
  (or (and to (utils.hex/valid-hex? to)) (and data (not= data "0x"))))

;;Handlers

;TRANSACTION QUEUED signal from status-go
(handlers/register-handler-fx
  :transaction-queued
  [(re-frame/inject-cofx :now)]
  (fn [{{:wallet/keys [send-transaction] :as db} :db now :now} [_ {:keys [id message_id args] :as transaction}]]
    (if (transaction-valid? transaction)
      (let [{:keys [from to value data gas gasPrice]} args]
        (let [;;TODO (andrey) revisit this map later (this map from old transactions, idk if we need all these fields)
              transaction {:id         id
                           :from       from
                           :to         to
                           :value      (money/to-decimal value)
                           :data       data
                           :gas        (money/to-decimal gas)
                           :gas-price  (money/to-decimal gasPrice)
                           :timestamp  now
                           :message-id message_id}]
          (merge
            {:db (-> db
                     (assoc-in [:wallet :transactions-unsigned id] transaction)
                     (assoc-in [:wallet/send-transaction :transaction-id] id))}
            (if (:waiting-signal? send-transaction)
              ;;sending from wallet
              {:dispatch [:wallet.send-transaction/transaction-queued id]}
              ;;sending from chat
              {:dispatch [:navigate-to-modal :wallet-send-transaction-modal {:amount (str (money/wei->ether value))
                                                                             :transaction-id id
                                                                             :to-address to
                                                                             :to-name to}]}))))
      {:discard-transaction id})))

;TRANSACTION FAILED signal from status-go
(handlers/register-handler-fx
  :transaction-failed
  (fn [{{:accounts/keys [accounts current-account-id] :as db} :db} [_ {:keys [id args message_id error_code error_message] :as event}]]
    (let [current-account-address (:address (get accounts current-account-id))
          transaction-initiator-address (utils.hex/normalize-hex (:from args))]
      (case error_code

        ;;WRONG PASSWORD
        constants/send-transaction-password-error-code
        {:db (assoc-in db [:wallet/send-transaction :wrong-password?] true)}

        ;;TODO (andrey) something weird here below, revisit
        ;;DISCARDED
        constants/send-transaction-discarded-error-code
        {:db       (update-in db [:wallet :transactions-unsigned] dissoc id)
         :dispatch [:set-chat-ui-props {:validation-messages nil}]}

        ;;NO ERROR, TIMEOUT or DEFAULT ERROR
        (merge
          {:db (update-in db [:wallet :transactions-unsigned] dissoc id)}
          (when (and message_id (= current-account-address transaction-initiator-address))
            {:dispatch [:set-chat-ui-props {:validation-messages error_message}]}))))))