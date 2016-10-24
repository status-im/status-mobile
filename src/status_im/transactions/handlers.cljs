(ns status-im.transactions.handlers
  (:require [re-frame.core :refer [after dispatch debug enrich]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.navigation.handlers :as nav]
            [status-im.utils.handlers :as u]
            [status-im.utils.types :as t]
            [status-im.components.status :as status]
            [clojure.string :as s]
            [taoensso.timbre :as log]))

;; flow:
;; :accept-transactions
;;          ↓
;; :transaction-completed
;;          ↓
;; ::remove-transaction && [:set :wrong-password? false]  <- on error
;; ::remove-transaction                                   <- when transaction is
;;                                                           not from the jail
;; ::add-transactions-hash
;; && ::check-completed-transaction!
;; && :navigation-replace                                 <- on success


(defmethod nav/preload-data! :confirm
  [{:keys [transactions-queue] :as db} _]
  (assoc db :transactions transactions-queue))

(defn on-unlock
  [ids password previous-view-id]
  (dispatch [:set :wrong-password? false])
  (doseq [id ids]
    (status/complete-transaction
      id
      password
      #(dispatch [:transaction-completed
                  {:id               id
                   :response         %
                   :previous-view-id previous-view-id}]))))

(register-handler :accept-transactions
  (u/side-effect!
    (fn [{:keys [transactions navigation-stack]} [_ password]]
      (let [ids              (keys transactions)
            previous-view-id (second navigation-stack)]
        (on-unlock ids password previous-view-id)))))

(register-handler :deny-transactions
  (u/side-effect!
    (fn [{:keys [transactions]}]
      (let [transactions' (vals transactions)
            messages-ids  (map :message-id transactions')
            ids           (map :id transactions')]
        (dispatch [::remove-pending-messages messages-ids])
        (dispatch [::remove-transactions ids])
        (dispatch [:navigate-back])))))

(register-handler :deny-transaction
  (u/side-effect!
    (fn [{:keys [transactions]} [_ id]]
      (let [{:keys [message-id] :as transaction} (get transactions id)]
        (when transaction
          (dispatch [::remove-pending-message message-id])
          (dispatch [::remove-transaction id]))))))

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

(defn mark-command-as-pending [db chat-id id]
  (let [path [:chats chat-id :staged-commands id]]
    (if (get-in db path)
      (update-in db path assoc :pending true)
      db)))

(register-handler :wait-for-transaction
  (after (fn [_ [_ message-id]]
           (dispatch [::check-completed-transaction!
                      {:message-id message-id}])))
  (fn [db [_ message-id {:keys [chat-id command] :as params}]]
    (let [id (:id command)]
      (-> db
          (mark-command-as-pending chat-id id)
          (assoc-in [:transaction-subscribers message-id] params)))))

(defn remove-pending-message
  [{:keys [command->chat] :as db} message-id]
  (let [chat-id (get command->chat message-id)
        path    [:chats chat-id :staged-commands]]
    (if chat-id
      (-> db
          (update :transaction-subscribers dissoc message-id)
          (update-in path dissoc message-id))
      db)))

(register-handler ::remove-pending-messages
  (fn [db [_ ids]]
    (log/debug :message-ids ids)
    (reduce remove-pending-message db ids)))

(register-handler ::remove-pending-message
  (fn [db [_ message-id]]
    (remove-pending-message db message-id)))

(register-handler :transaction-queued
  (after #(dispatch [:navigate-to :confirm]))
  (fn [db [_ {:keys [id message_id args]}]]
    (let [{:keys [from to value]} args
          transaction {:id         id
                       :from       from
                       :to         to
                       :value      (.toDecimal js/Web3.prototype value)
                       :message-id message_id}]
      (assoc-in db [:transactions-queue id] transaction))))

(register-handler :transaction-completed
  (u/side-effect!
    (fn [{:keys [transactions command->chat]} [_ {:keys [id response previous-view-id]}]]
      (let [{:keys [hash error] :as parsed-response} (t/json->clj response)
            {:keys [message-id]} (transactions id)]
        (log/debug :parsed-response parsed-response)
        (if (and error (string? error) (not (s/blank? error)))
          ;; todo: revisit this
          ;; currently transaction is removed after attempt
          ;; to complete it with wrong password
          (do
            (dispatch [::remove-transaction id])
            (dispatch [:set :wrong-password? true])
            (when-let [chat-id (get command->chat message-id)]
              (dispatch [:clear-command chat-id message-id])))
          (if message-id
            (do (dispatch [::add-transactions-hash {:id         id
                                                    :hash       hash
                                                    :message-id message-id}])
                (dispatch [::check-completed-transaction!
                           {:message-id message-id}])
                (dispatch [:navigation-replace previous-view-id]))
            (dispatch [::remove-transaction id])))))))

(register-handler ::add-transactions-hash
  (fn [db [_ {:keys [id hash message-id]}]]
    (-> db
        (assoc-in [:transactions id :hash] hash)
        (assoc-in [:message-id->transaction-id message-id] id))))

(register-handler ::send-pending-message
  (u/side-effect!
    (fn [{:keys [transaction-subscribers]} [_ message-id hash]]
      (when-let [params (transaction-subscribers message-id)]
        (let [params' (assoc-in params [:handler-data :transaction-hash] hash)]
          (dispatch [:prepare-command! params']))
        (dispatch [::remove-transaction-subscriber message-id])))))

(register-handler ::remove-transaction-subscriber
  (fn [db [_ old-hash]]
    (update db :transaction-subscribers dissoc old-hash)))

(register-handler ::check-completed-transaction!
  (u/side-effect!
    (fn [{:keys [message-id->transaction-id transactions transaction-subscribers]}
         [_ {:keys [message-id]}]]
      (let [id              (get message-id->transaction-id message-id)
            {:keys [hash]} (get transactions id)
            pending-message (get transaction-subscribers message-id)]
        (when (and pending-message id hash)
          (dispatch [::send-pending-message message-id hash])
          (dispatch [::remove-transaction id]))))))
