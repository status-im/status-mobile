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

;;TRANSACTION QUEUED signal from status-go
(handlers/register-handler-fx
  :transaction-queued
  [(re-frame/inject-cofx :now)]
  (fn [{:keys [db now]} [_ {:keys [id message_id args] :as transaction}]]
    (if (transaction-valid? transaction)
      (let [{:keys [from to value data gas gasPrice]} args
            ;;TODO (andrey) revisit this map later (this map from old transactions, idk if we need all these fields)
            transaction                               {:id         id
                                                       :from       from
                                                       :to         to
                                                       :value      (money/bignumber value)
                                                       :data       data
                                                       :gas        (money/to-decimal gas)
                                                       :gas-price  (money/to-decimal gasPrice)
                                                       :timestamp  now
                                                       :message-id message_id}
            sending-from-chat?                        (not (get-in db [:wallet :send-transaction :waiting-signal?]))
            new-db                                    (assoc-in db [:wallet :transactions-unsigned id] transaction)
            sending-db                                {:id         id
                                                       :from-chat? sending-from-chat?}] 
        (if sending-from-chat?
          {:db        (assoc-in new-db [:wallet :send-transaction] sending-db) ; we need to completly reset sending state here
           :dispatch [:navigate-to-modal :wallet-send-transaction-modal]}
          {:db       (update-in new-db [:wallet :send-transaction] merge sending-db) ; just update sending state as we are in wallet flow
           :dispatch [:wallet.send-transaction/transaction-queued id]}))
      {:discard-transaction id})))

;;TRANSACTION FAILED signal from status-go
(handlers/register-handler-fx
  :transaction-failed
  (fn [{{:accounts/keys [accounts current-account-id] :as db} :db} [_ {:keys [id args message_id error_code error_message] :as event}]]
    (let [current-account-address (:address (get accounts current-account-id))
          transaction-initiator-address (utils.hex/normalize-hex (:from args))]
      (case error_code

        ;;WRONG PASSWORD
        constants/send-transaction-password-error-code
        {:db (assoc-in db [:wallet :send-transaction :wrong-password?] true)}

        ;;TODO (andrey) something weird here below, revisit
        ;;DISCARDED
        constants/send-transaction-discarded-error-code
        {:db       (update-in db [:wallet :transactions-unsigned] dissoc id)
         :dispatch [:set-chat-ui-props {:validation-messages nil}]}

        constants/send-transaction-timeout-error-code
        {:db (update-in db [:wallet :transactions-unsigned] dissoc id)}

        nil))))