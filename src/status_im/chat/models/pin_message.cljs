(ns status-im.chat.models.pin-message
  (:require [status-im.chat.models.message-list :as message-list]
            [status-im.constants :as constants]
            [status-im.data-store.pin-messages :as data-store.pin-messages]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [re-frame.core :as re-frame]))

(fx/defn handle-failed-loading-pin-messages
  {:events [::failed-loading-pin-messages]}
  [{:keys [db]} current-chat-id _ err]
  (log/error "failed loading pin messages" current-chat-id err)
  (when current-chat-id
    {:db (assoc-in db [:pagination-info current-chat-id :loading-pin-messages?] false)}))

(fx/defn pin-messages-loaded
  {:events [::pin-messages-loaded]}
  [{db :db} chat-id {:keys [cursor pinned-messages]}]
  (let [all-messages (reduce (fn [acc {:keys [message-id] :as message}]
                               (assoc acc message-id message))
                             {}
                             pinned-messages)
        messages-id-list (map :message-id pinned-messages)]
    {:db (-> db
             (assoc-in [:pagination-info chat-id :loading-pin-messages?] false)
             (assoc-in [:pin-messages chat-id] all-messages)
             (assoc-in [:pin-message-lists chat-id] (message-list/add-many nil (vals all-messages)))
             (assoc-in [:pagination-info chat-id :all-pin-loaded?]
                       (empty? cursor)))}))

(fx/defn receive-signal
  [{:keys [db] :as cofx} pin-messages]
  (let [{:keys [chat-id]} (first pin-messages)]
    (when (= chat-id (db :current-chat-id))
      (let [{:keys [chat-id]} (first pin-messages)
            already-loaded-pin-messages (get-in db [:pin-messages chat-id] {})
            already-loaded-messages (get-in db [:messages chat-id] {})
            all-messages (reduce (fn [acc {:keys [message_id pinned from]}]
                                   ;; Add to or remove from pinned message list, and normalizing pinned-by property
                                   (let [current-message (get already-loaded-messages message_id)
                                         current-message-pin (merge current-message
                                                                    {:pinned    pinned
                                                                     :pinned-by from})]
                                     (cond-> acc
                                       (nil? pinned)
                                       (dissoc message_id)

                                       (and (some? pinned) (some? current-message))
                                       (assoc message_id current-message-pin))))
                                 already-loaded-pin-messages
                                 pin-messages)]
        {:db (-> db
                 (assoc-in [:pin-messages chat-id] all-messages)
                 (assoc-in [:pin-message-lists chat-id]
                           (message-list/add-many nil (vals all-messages))))}))))

(fx/defn load-more-pin-messages
  [{:keys [db]} chat-id first-request]
  (let [not-all-loaded? (not (get-in db [:pagination-info chat-id :all-loaded?]))
        not-loading-pin-messages? (not (get-in db [:pagination-info chat-id :loading-pin-messages?]))]
    (when not-loading-pin-messages?
      (fx/merge
       {:db (assoc-in db [:pagination-info chat-id :loading-pin-messages?] true)}
       (data-store.pin-messages/pinned-message-by-chat-id-rpc
        chat-id
        nil
        constants/default-number-of-pin-messages
        #(re-frame/dispatch [::pin-messages-loaded chat-id %])
        #(re-frame/dispatch [::failed-loading-pin-messages chat-id %]))))))

(fx/defn send-pin-message
  "Pin message, rebuild pinned messages list"
  {:events [::send-pin-message]}
  [{:keys [db] :as cofx} {:keys [chat-id message-id pinned] :as pin-message}]
  (let [current-public-key (get-in db [:multiaccount :public-key])
        message (merge pin-message {:pinned-by current-public-key})]
    (fx/merge cofx
              {:db (cond-> db
                     pinned
                     (->
                      (update-in [:pin-message-lists chat-id] message-list/add message)
                      (assoc-in [:pin-messages chat-id message-id] message))
                     (not pinned)
                     (->
                      (update-in [:pin-message-lists chat-id] message-list/remove-message pin-message)
                      (update-in [:pin-messages chat-id] dissoc message-id)))}
              (data-store.pin-messages/send-pin-message {:chat-id    (pin-message :chat-id)
                                                         :message_id (pin-message :message-id)
                                                         :pinned     (pin-message :pinned)}))))

(fx/defn load-pin-messages
  {:events [::load-pin-messages]}
  [{:keys [db] :as cofx} chat-id]
  (load-more-pin-messages cofx chat-id true))
