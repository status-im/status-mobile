(ns status-im.transactions.handlers
  (:require [re-frame.core :refer [after dispatch debug enrich]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.navigation.handlers :as nav]
            [status-im.utils.handlers :as u]
            [status-im.utils.types :as t]
            [status-im.components.geth :as g]
            cljsjs.web3
            [clojure.string :as s]))

(defmethod nav/preload-data! :confirm
  [{:keys [transactions-queue] :as db} _]
  (assoc db :transactions transactions-queue))

(defn on-unlock [hashes]
  (fn [result-str]
    (let [{:keys [error]} (t/json->clj result-str)]
      ;; todo: add message about wrong password
      (if (s/blank? error)
        (do
          (dispatch [:set :wrong-password? false])
          (doseq [hash hashes]
            (g/complete-transaction
              hash
              #(dispatch [:transaction-completed hash %])))
          (dispatch [:navigate-back]))
        (dispatch [:set :wrong-password? true])))))

(register-handler :accept-transactions
  (u/side-effect!
    (fn [{:keys [transactions current-account-id]} [_ password]]
      (let [hashes (keys transactions)]
        (g/login current-account-id password (on-unlock hashes))))))

(register-handler :deny-transactions
  (u/side-effect!
    (fn [{:keys [transactions]}]
      (let [hashes (keys transactions)]
        (dispatch [::remove-pending-messages hashes])
        (dispatch [::remove-trqqansactions hashes])
        (dispatch [:navigate-back])))))

(register-handler :deny-transaction
  (u/side-effect!
    (fn [_ [_ hash]]
      (dispatch [::remove-pending-message hash])
      (dispatch [::remove-transaction hash]))))

(register-handler ::remove-transactions
  (fn [db [_ hashes]]
    (-> db
        (dissoc :transactions)
        (update :transactions-queue #(apply dissoc % hashes)))))

(register-handler ::remove-transaction
  (fn [db [_ hash]]
    (-> db
        (update :transactions dissoc hash)
        (update :transactions-queue dissoc hash))))

(register-handler :wait-for-transaction
  (fn [db [_ hash {:keys [chat-id command] :as params}]]
    (let [id (:id command)]
      (-> db
          (update-in [:chats chat-id :staged-commands id] assoc :pending true)
          (assoc-in [:transaction-subscribers hash] params)))))

(defn remove-pending-message [db hash]
  (let [{:keys [chat-id command]} (get-in db [:transaction-subscribers hash])
        path [:chats chat-id :staged-commands]]
    (-> db
        (update :transaction-subscribers dissoc hash)
        (update-in path dissoc (:id command)))))

(register-handler ::remove-pending-messages
  (fn [db [_ hashes]]
    (reduce remove-pending-message db hashes)))

(register-handler ::remove-pending-message
  (fn [db [_ hash]]
    (remove-pending-message db hash)))

(register-handler :signal-event
  (u/side-effect!
    (fn [_ [_ event-str]]
      (let [{:keys [type event]} (t/json->clj event-str)]
        (case type
          "sendTransactionQueued" (dispatch [:transaction-queued event]))))))

(register-handler :transaction-queued
  (after #(dispatch [:navigate-to :confirm]))
  (fn [db [_ {:keys [hash args]}]]
    (let [{:keys [from to value]} args
          transaction {:hash  hash
                       :from  from
                       :to    to
                       :value (.toDecimal js/Web3.prototype value)}]
      (assoc-in db [:transactions-queue hash] transaction))))

(register-handler :transaction-completed
  (u/side-effect!
    (fn [db [_ old-hash result-str]]
      (let [{:keys [hash error]} (t/json->clj result-str)]
        ;; todo: handle error
        (when hash
          (dispatch [::send-pending-message old-hash hash])
          (dispatch [::remove-transaction old-hash]))))))

(register-handler ::send-pending-message
  (u/side-effect!
    (fn [{:keys [transaction-subscribers] :as db} [_ old-hash new-hash]]
      (when-let [params (transaction-subscribers old-hash)]
        (let [params' (assoc-in params [:handler-data :transaction-hash] new-hash)]
          (dispatch [:prepare-command! params']))
        (dispatch [::remove-transaction-subscriber old-hash])))))

(register-handler ::remove-transaction-subscriber
  (fn [db [_ old-hash]]
    (update db :transaction-subscribers dissoc old-hash)))
