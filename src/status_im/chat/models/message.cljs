(ns status-im.chat.models.message
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.chat.models.loading :as chat.loading]
            [status-im.data-store.messages :as data-store.messages]
            [status-im.transport.message.protocol :as protocol]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [status-im2.contexts.chat.messages.delete-message.events :as delete-message]
            [status-im2.contexts.chat.messages.list.events :as message-list]
            [status-im2.contexts.chat.messages.list.state :as view.state]
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]))

(defn- message-loaded?
  [db chat-id message-id]
  (get-in db [:messages chat-id message-id]))

(defn- earlier-than-deleted-at?
  [db chat-id clock-value]
  (>= (get-in db [:chats chat-id :deleted-at-clock-value]) clock-value))

(defn add-timeline-message
  [acc chat-id message-id message]
  (-> acc
      (update-in [:db :messages chat-id] assoc message-id message)
      (update-in [:db :message-lists chat-id] message-list/add message)))

(defn hide-message
  "Hide chat message, rebuild message-list"
  [{:keys [db]} chat-id message-id]
  ;; TODO this is too expensive, probably we could mark message somehow and just hide it in the UI
  (message-list/rebuild-message-list {:db (update-in db [:messages chat-id] dissoc message-id)} chat-id))

(defn add-pinned-message
  [acc chat-id message-id message]
  (let [{:keys [pinned-by pinned-at] :as pinned-message}
        (get-in acc [:db :pin-messages chat-id message-id])]
    (if pinned-message
      (assoc-in acc
       [:db :pin-messages chat-id message-id]
       (assoc
        message
        :pinned-by pinned-by
        :pinned-at pinned-at))
      acc)))

(defn add-message
  [{:keys [db] :as acc} message-js chat-id message-id cursor-clock-value]
  (let [{:keys [from clock-value] :as message}
        (data-store.messages/<-rpc (types/js->clj message-js))
        acc-with-pinned-message (add-pinned-message acc chat-id message-id message)]
    (if (message-loaded? db chat-id message-id)
      ;; If the message is already loaded, it means it's an update, that
      ;; happens when a message that was missing a reply had the reply
      ;; coming through, in which case we just insert the new message
      (assoc-in acc-with-pinned-message [:db :messages chat-id message-id] message)
      (cond-> acc-with-pinned-message
        ;;add new message to db
        :always
        (update-in [:db :messages chat-id] assoc message-id message)
        :always
        (update-in [:db :message-lists chat-id] message-list/add message)

        (or (not cursor-clock-value) (< clock-value cursor-clock-value))
        (update-in [:db :pagination-info chat-id]
                   assoc
                   :cursor             (chat.loading/clock-value->cursor clock-value)
                   :cursor-clock-value clock-value)

        ;;conj sender for add-sender-to-chat-users
        (not (get-in db [:chats chat-id :users from]))
        (update :senders assoc from message)

        (not (string/blank? (:replace message)))
        ;;TODO this is expensive
        (hide-message chat-id (:replace message))))))

(defn reduce-js-messages
  [{:keys [db] :as acc} ^js message-js]
  (let [chat-id            (.-localChatId message-js)
        clock-value        (.-clock message-js)
        message-id         (.-id message-js)
        current-chat-id    (:current-chat-id db)
        cursor-clock-value (get-in db [:pagination-info current-chat-id :cursor-clock-value])]
    ;;ignore not opened chats and earlier clock
    (if (and (get-in db [:pagination-info chat-id :messages-initialized?])
             ;;TODO why do we need this ?
             (not (earlier-than-deleted-at? db chat-id clock-value)))
      (if (or (not @view.state/first-not-visible-item)
              (<= (:clock-value @view.state/first-not-visible-item)
                  clock-value))
        (add-message acc message-js chat-id message-id cursor-clock-value)
        ;; Not in the current view, set all-loaded to false
        ;; and offload to db and update cursor if necessary
        ;;TODO if we'll offload messages , it will conflict with end reached, so probably if we
        ;;reached the end of visible area,
        ;; we need to drop other messages with (< clock-value cursor-clock-value) from response-js
        ;; so we don't update :cursor-clock-value because it will be changed when we loadMore
        ;; message
        {:db (cond-> (assoc-in db [:pagination-info chat-id :all-loaded?] false)
               (> clock-value cursor-clock-value)
               ;;TODO cut older messages from messages-list
               (update-in [:pagination-info chat-id]
                          assoc
                          :cursor             (chat.loading/clock-value->cursor clock-value)
                          :cursor-clock-value clock-value))})
      acc)))

(defn receive-many
  [{:keys [db]} ^js response-js]
  (let [messages-js ^js (.splice (.-messages response-js) 0 (if platform/low-device? 3 10))
        {:keys [db]}
        (reduce reduce-js-messages
                {:db db :chats #{} :senders {} :transactions #{}}
                messages-js)]
    ;;we want to render new messages as soon as possible so we dispatch later all other events which
    ;;can be handled async
    {:db db
     :utils/dispatch-later
     (concat [{:ms 20 :dispatch [:process-response response-js]}]
             (when (and (:current-chat-id db) (= "active" (:app-state db)))
               [{:ms 100 :dispatch [:chat/mark-all-as-read (:current-chat-id db)]}]))}))

(rf/defn update-db-message-status
  [{:keys [db] :as cofx} chat-id message-id status]
  (when (get-in db [:messages chat-id message-id])
    (rf/merge cofx
              {:db (assoc-in db
                    [:messages chat-id message-id :outgoing-status]
                    status)})))

(rf/defn update-message-status
  [{:keys [db] :as cofx} chat-id message-id status]
  (rf/merge cofx
            (update-db-message-status chat-id message-id status)))

(rf/defn resend-message
  [{:keys [db] :as cofx} chat-id message-id]
  (rf/merge cofx
            {:json-rpc/call [{:method     "wakuext_reSendChatMessage"
                              :params     [message-id]
                              :on-success #(log/debug "re-sent message successfully")
                              :on-error   #(log/error "failed to re-send message" %)}]}
            (update-message-status chat-id message-id :sending)))

(rf/defn send-message
  [cofx message]
  (protocol/send-chat-messages cofx [message]))

(rf/defn send-messages
  [cofx messages]
  (protocol/send-chat-messages cofx messages))

(rf/defn handle-removed-messages
  {:events [::handle-removed-messages]}
  [{:keys [db] :as cofx} removed-messages]
  (let [mark-as-deleted-fx (->> removed-messages
                                (map #(assoc %
                                             :message-id (:messageId %)
                                             :deleted-by (:deletedBy %)))
                                (group-by :chatId)
                                (mapv (fn [[chat-id messages]]
                                        (delete-message/delete-messages-localy messages chat-id))))
        mark-as-seen-fx    (mapv
                            (fn [removed-message]
                              (let [chat-id    (:chatId removed-message)
                                    message-id (:messageId removed-message)]
                                (data-store.messages/mark-messages-seen chat-id
                                                                        [message-id]
                                                                        #(re-frame/dispatch
                                                                          [:chat/decrease-unviewed-count
                                                                           chat-id %3]))))
                            removed-messages)
        remove-messages-fx (fn [{:keys [db]}]
                             {:dispatch [:activity-center.notifications/fetch-unread-count]})]
    (apply rf/merge
           cofx
           (-> mark-as-deleted-fx
               (concat mark-as-seen-fx)
               (conj remove-messages-fx)))))

(comment
  (handle-removed-messages
   {:db {:messages {:c1 {:m1 {:chat-id :c1 :message-id :m1}
                         :m2 {:chat-id :c1 :message-id :m2}}
                    :c2 {:m3 {:chat-id :c2 :message-id :m3}
                         :m4 {:chat-id :c2 :message-id :m4}}}}}
   [:m1 :m3]))

(defn remove-cleared-message
  [messages cleared-at]
  (into {}
        (remove #(let [message-clock (:clock-value (second %))]
                   (<= message-clock cleared-at))
                messages)))

(rf/defn handle-cleared-histories-messages
  {:events [::handle-cleared-hisotories-messages]}
  [{:keys [db]} cleared-histories]
  {:db (reduce (fn [acc current]
                 (update-in acc
                            [:messages (:chatId current)]
                            remove-cleared-message
                            (:clearedAt current)))
               db
               cleared-histories)})
