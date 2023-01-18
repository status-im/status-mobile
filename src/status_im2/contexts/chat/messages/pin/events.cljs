(ns status-im2.contexts.chat.messages.pin.events
  (:require [re-frame.core :as re-frame]
            [status-im2.contexts.chat.messages.list.events :as message-list]
            [status-im2.constants :as constants]
            [status-im.data-store.pin-messages :as data-store.pin-messages]
            [status-im.transport.message.protocol :as protocol]
            [utils.re-frame :as rf]
            [taoensso.timbre :as log]))

(rf/defn handle-failed-loading-pin-messages
  {:events [:pin-message/failed-loading-pin-messages]}
  [{:keys [db]} current-chat-id _ err]
  (log/error "failed loading pin messages" current-chat-id err)
  (when current-chat-id
    {:db (assoc-in db [:pagination-info current-chat-id :loading-pin-messages?] false)}))

(rf/defn pin-messages-loaded
  {:events [:pin-message/pin-messages-loaded]}
  [{db :db} chat-id {:keys [cursor pinned-messages]}]
  (let [all-messages (reduce (fn [acc {:keys [message-id] :as message}]
                               (assoc acc message-id message))
                             {}
                             pinned-messages)]
    {:db (-> db
             (assoc-in [:pagination-info chat-id :loading-pin-messages?] false)
             (assoc-in [:pin-messages chat-id] all-messages)
             (assoc-in [:pin-message-lists chat-id] (message-list/add-many nil (vals all-messages)))
             (assoc-in [:pagination-info chat-id :all-pin-loaded?]
                       (empty? cursor)))}))

(rf/defn receive-signal
  [{:keys [db]} pin-messages]
  (let [{:keys [chat-id]} (first pin-messages)]
    (when (= chat-id (db :current-chat-id))
      (let [{:keys [chat-id]}           (first pin-messages)
            already-loaded-pin-messages (get-in db [:pin-messages chat-id] {})
            already-loaded-messages     (get-in db [:messages chat-id] {})
            all-messages                (reduce (fn [acc {:keys [message_id pinned from]}]
                                                  ;; Add to or remove from pinned message list, and
                                                  ;; normalizing pinned-by property
                                                  (let [current-message     (get already-loaded-messages
                                                                                 message_id)
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

(rf/defn send-pin-message
  "Pin message, rebuild pinned messages list"
  {:events [:pin-message/send-pin-message]}
  [{:keys [db] :as cofx} {:keys [chat-id message-id pinned] :as pin-message}]
  (let [current-public-key (get-in db [:multiaccount :public-key])
        message            (merge pin-message {:pinned-by current-public-key})
        preferred-name     (get-in db [:multiaccount :preferred-name])]
    (rf/merge cofx
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
                                                         :pinned     (pin-message :pinned)})
              (when pinned
                (protocol/send-chat-messages [{:chat-id      (pin-message :chat-id)
                                               :content-type constants/content-type-system-text
                                               :text         "pinned a message"
                                               :response-to  (pin-message :message-id)
                                               :ens-name     preferred-name}])))))

(rf/defn load-pin-messages
  {:events [:pin-message/load-pin-messages]}
  [{:keys [db]} chat-id]
  (let [not-loading-pin-messages? (not (get-in db [:pagination-info chat-id :loading-pin-messages?]))]
    (when not-loading-pin-messages?
      (rf/merge
       {:db (assoc-in db [:pagination-info chat-id :loading-pin-messages?] true)}
       (data-store.pin-messages/pinned-message-by-chat-id-rpc
        chat-id
        nil
        constants/default-number-of-pin-messages
        #(re-frame/dispatch [:pin-message/pin-messages-loaded chat-id %])
        #(re-frame/dispatch [:pin-message/failed-loading-pin-messages chat-id %]))))))

(rf/defn show-pin-limit-modal
  {:events [:pin-message/show-pin-limit-modal]}
  [{:keys [db]} chat-id]
  {:db (assoc-in db [:pin-modal chat-id] true)})

(rf/defn hide-pin-limit-modal
  {:events [:pin-message/hide-pin-limit-modal]}
  [{:keys [db]} chat-id]
  {:db (assoc-in db [:pin-modal chat-id] false)})
