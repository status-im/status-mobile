(ns status-im2.contexts.chat.messages.pin.events
  (:require
    [legacy.status-im.data-store.messages :as data-store.messages]
    [legacy.status-im.data-store.pin-messages :as data-store.pin-messages]
    [quo.foundations.colors :as colors]
    [re-frame.core :as re-frame]
    [status-im2.common.toasts.events :as toasts]
    [status-im2.constants :as constants]
    [status-im2.contexts.chat.menus.pinned-messages.view :as pinned-messages-menu]
    [status-im2.contexts.chat.messages.list.events :as message-list]
    [status-im2.navigation.events :as navigation]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

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

(defn remove-pinned-message
  [db pinned-message]
  (update-in db
             [:pin-messages (aget pinned-message "localChatId")]
             dissoc
             (aget pinned-message "message_id")))

(defn add-pinned-message
  [db pinned-message]
  (let [message (aget pinned-message "pinnedMessage")]
    (if (and message
             (aget message "message"))
      (assoc-in db
       [:pin-messages
        (aget pinned-message "localChatId")
        (aget pinned-message "message_id")]
       (-> (aget message "message")
           (js->clj :keywordize-keys true)
           data-store.messages/<-rpc
           (assoc :pinned-by (aget message "pinnedBy")
                  :pinned-at (aget message "pinnedAt"))))
      db)))

(rf/defn receive-signal
  [{:keys [db]} pin-messages]
  (let [current-chat-id (db :current-chat-id)
        db              (reduce (fn [db pin-message]
                                  (let [pinned? (aget pin-message "pinned")
                                        chat-id (aget pin-message "localChatId")]
                                    (cond
                                      (not= chat-id current-chat-id)
                                      db
                                      pinned?
                                      (add-pinned-message db pin-message)
                                      :else
                                      (remove-pinned-message db pin-message))))
                                db
                                pin-messages)]
    {:db
     (assoc-in db
      [:pin-message-lists current-chat-id]
      (message-list/add-many nil (vals (get-in db [:pin-messages current-chat-id]))))}))

(rf/defn send-pin-message-locally
  "Pin message, rebuild pinned messages list locally"
  {:events [:pin-message/send-pin-message-locally]}
  [{:keys [db] :as cofx} {:keys [chat-id message-id pinned] :as pin-message}]
  (let [current-public-key       (get-in db [:profile/profile :public-key])
        message                  (merge pin-message {:pinned-by current-public-key})
        pin-message-lists-exist? (some? (get-in db [:pin-message-lists chat-id]))]
    (rf/merge cofx
              {:db (cond-> db
                     pinned
                     (->
                       (update-in [:pin-message-lists chat-id] message-list/add message)
                       (assoc-in [:pin-messages chat-id message-id] message))
                     (and (not pinned) pin-message-lists-exist?)
                     (->
                       (update-in [:pin-message-lists chat-id] message-list/remove-message pin-message)
                       (update-in [:pin-messages chat-id] dissoc message-id)))})))

(rf/defn send-pin-message
  "Pin message, rebuild pinned messages list"
  {:events [:pin-message/send-pin-message]}
  [{:keys [db] :as cofx} {:keys [chat-id message-id pinned remote-only?] :as pin-message}]
  (rf/merge cofx
            (when-not remote-only? (send-pin-message-locally pin-message))
            (data-store.pin-messages/send-pin-message {:chat-id    (pin-message :chat-id)
                                                       :message_id (pin-message :message-id)
                                                       :pinned     (pin-message :pinned)})))

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
  [cofx]
  (toasts/upsert cofx
                 {:icon       :alert
                  :icon-color colors/danger-50
                  :text       (i18n/label :t/pin-limit-reached)}))

(rf/defn show-pins-bottom-sheet
  {:events [:pin-message/show-pins-bottom-sheet]}
  [cofx chat-id]
  (navigation/show-bottom-sheet cofx {:content (fn [] [pinned-messages-menu/pinned-messages chat-id])}))
