(ns status-im.transactions.handlers
  (:require [re-frame.core :refer [after dispatch debug enrich]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.navigation.handlers :as nav]
            [status-im.utils.datetime :as time]
            [status-im.utils.handlers :as u]
            [status-im.utils.types :as t]
            [status-im.utils.hex :refer [valid-hex?]]
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


(defmethod nav/preload-data! :unsigned-transactions
  [{:keys [transactions-queue] :as db} _]
  (-> db
      (assoc :transactions transactions-queue
             :wrong-password-counter 0
             :wrong-password? false)
      (assoc-in [:confirm-transactions :password] "")))

(defmethod nav/preload-data! :transaction-details
  [db [_ _ transaction]]
  (-> db
      (assoc :selected-transaction transaction
             :wrong-password-counter 0
             :wrong-password? false)
      (assoc-in [:confirm-transactions :password] "")))

(defn on-transactions-completed [raw-results]
  (let [results (:results (t/json->clj raw-results))]
    (doseq [result results]
      (dispatch [:transaction-completed {:id (name (key result)) :response (second result)}]))))

(register-handler :accept-transactions
  (u/side-effect!
   (fn [{:keys [transactions]} [_ password]]
     (dispatch [:set :wrong-password? false])
     (status/complete-transactions (keys transactions) password on-transactions-completed))))

(register-handler :accept-transaction
  (u/side-effect!
   (fn [_ [_ password id]]
     (dispatch [:set :wrong-password? false])
     (status/complete-transactions (list id) password on-transactions-completed))))

(register-handler :deny-transactions
  (u/side-effect!
    (fn [{:keys [transactions]}]
      (let [transactions' (vals transactions)
            messages-ids  (map :message-id transactions')
            ids           (map :id transactions')]
        (dispatch [::remove-pending-messages messages-ids])
        (dispatch [::remove-transactions ids])
        (doseq [id ids]
          (dispatch [::discard-transaction id]))))))

(register-handler :deny-transaction
  (u/side-effect!
    (fn [{:keys [transactions]} [_ id]]
      (let [{:keys [message-id] :as transaction} (get transactions id)]
        (when transaction
          (dispatch [::remove-pending-message message-id])
          (dispatch [::remove-transaction id])
          (dispatch [::discard-transaction id]))))))

(register-handler ::discard-transaction
  (u/side-effect!
    (fn [_ [_ id]]
      (status/discard-transaction id))))

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
  (after (fn [_ [_ message-id]]
           (dispatch [::check-completed-transaction!
                      {:message-id message-id}])))
  (fn [db [_ message-id params]]
    (assoc-in db [:transaction-subscribers message-id] params)))

(defn remove-pending-message
  [{:keys [command->chat] :as db} message-id]
  (let [chat-id (get command->chat message-id)]
    (if chat-id
      (update db :transaction-subscribers dissoc message-id)
      db)))

(register-handler ::remove-pending-messages
  (fn [db [_ ids]]
    (log/debug :message-ids ids)
    (reduce remove-pending-message db ids)))

(register-handler ::remove-pending-message
  (fn [db [_ message-id]]
    (remove-pending-message db message-id)))

(register-handler :transaction-queued
  (u/side-effect!
    (fn [_ [_ {:keys [id args] :as transaction}]]
      (if (:to args)
        (dispatch [::transaction-queued transaction])
        (status/discard-transaction id)))))

(register-handler ::transaction-queued
  (after #(dispatch [:navigate-to-modal :unsigned-transactions]))
  (fn [db [_ {:keys [id message_id args]}]]
    (let [{:keys [from to value data gas gasPrice]} args]
      (if (valid-hex? to)
        (let [transaction {:id         id
                           :from       from
                           :to         to
                           :value      (.toDecimal js/Web3.prototype value)
                           :data       data
                           :gas        (.toDecimal js/Web3.prototype gas)
                           :gas-price  (.toDecimal js/Web3.prototype gasPrice)
                           :timestamp  (time/now-ms)
                           :message-id message_id}]
          (assoc-in db [:transactions-queue id] transaction))
        db))))

(register-handler :transaction-completed
  (u/side-effect!
    (fn [{:keys [transactions modal]} [_ {:keys [id response]}]]
      (let [{:keys [hash error]} response
            {:keys [message-id]} (transactions id)]
        (log/debug :parsed-response response)
        (when-not (and error (string? error) (not (s/blank? error)))
          (if (and message-id (not (s/blank? message-id)))
            (do (dispatch [::add-transactions-hash {:id         id
                                                    :hash       hash
                                                    :message-id message-id}])
                (dispatch [::check-completed-transaction! {:message-id message-id}]))
            (dispatch [::remove-transaction id]))
          (when (#{:unsigned-transactions :transaction-details} modal)
            (dispatch [:navigate-to-modal :confirmation-success])))))))

(register-handler ::add-transactions-hash
  (fn [db [_ {:keys [id hash message-id]}]]
    (-> db
        (assoc-in [:transactions id :hash] hash)
        (assoc-in [:message-id->transaction-id message-id] id))))

(register-handler ::send-pending-message
  (u/side-effect!
    (fn [{:keys [transaction-subscribers]} [_ message-id hash]]
      (when-let [{:keys [chat-id] :as params} (transaction-subscribers message-id)]
        (let [params' (assoc-in params [:handler-data :transaction-hash] hash)]
          (dispatch [:prepare-command! chat-id params']))
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
          (dispatch [::send-pending-message message-id hash]))
        ;; todo revisit this
        (dispatch [::remove-transaction id])))))

(def wrong-password-code "2")
(def discard-code "4")

(register-handler :transaction-failed
  (u/side-effect!
    (fn [_ [_ {:keys [id message_id error_code error_message] :as event}]]
      (cond

        (= error_code wrong-password-code)
        (dispatch [:set-wrong-password!])

        (not= discard-code error_code)
        (do (when message_id
              (dispatch [::remove-pending-message message_id]))
            (dispatch [:clear-selected-transaction])
            (dispatch [::remove-transaction id])
            (dispatch [:set-chat-ui-props {:validation-messages error_message}]))

        :else
        (dispatch [:set-chat-ui-props {:validation-messages nil}])))))

(register-handler :clear-selected-transaction
  (fn [db _]
    (dissoc db :selected-transaction)))

(def attempts-limit 3)

(register-handler :set-wrong-password!
  (after (fn [{:keys [wrong-password-counter]}]
           (when (>= wrong-password-counter attempts-limit)
             (dispatch [:set :wrong-password? false])
             (dispatch [:set :wrong-password-counter 0])
             (dispatch [:set-in [:confirm-transactions :password] ""]))))
  (fn [db]
    (-> db
        (assoc :wrong-password? true)
        (update :wrong-password-counter (fnil inc 0)))))
