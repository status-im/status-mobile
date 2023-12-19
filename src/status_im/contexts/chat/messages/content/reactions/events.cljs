(ns status-im.contexts.chat.messages.content.reactions.events
  (:require [clojure.set :as set]
            [status-im.constants :as constants]
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]))

(defn update-reaction
  [acc retracted chat-id message-id emoji-id emoji-reaction-id reaction]
  ;; NOTE: For a better performance, better to not keep in db all retracted reactions
  ;; retraction will always come after the reaction so there shouldn't be a conflict
  (if retracted
    (update-in acc [chat-id message-id emoji-id] dissoc emoji-reaction-id)
    (assoc-in acc [chat-id message-id emoji-id emoji-reaction-id] reaction)))

(defn process-reactions
  [_]
  (fn [reactions new-reactions]
    ;; NOTE: handling own reaction in subscription could be expensive,
    ;; for better performance we can here separate own reaction into 2 maps
    (reduce
     (fn [acc
          {:keys [chat-id message-id emoji-id emoji-reaction-id retracted]
           :as   reaction}]
       (update-reaction acc retracted chat-id message-id emoji-id emoji-reaction-id reaction))
     reactions
     new-reactions)))

(defn- earlier-than-deleted-at?
  [{:keys [db]} {:keys [chat-id clock-value]}]
  (let [{:keys [deleted-at-clock-value]}
        (get-in db [:chats chat-id])]
    (>= deleted-at-clock-value clock-value)))

(rf/defn receive-signal
  [{:keys [db] :as cofx} reactions]
  (let [reactions (filter (partial earlier-than-deleted-at? cofx) reactions)]
    {:db (update db :reactions (process-reactions (:chats db)) reactions)}))

(defn <-rpc
  [message]
  (-> message
      (dissoc :chat_id)
      (set/rename-keys {:messageId   :message-id
                        :localChatId :chat-id
                        :emojiId     :emoji-id
                        :messageType :message-type
                        :id          :emoji-reaction-id})))

(rf/reg-event-fx :reactions/load-more
 (fn [{:keys [db]} [cursor chat-id]]
   (when-let [session-id (get-in db [:pagination-info chat-id :messages-initialized?])]
     {:json-rpc/call [{:method     "wakuext_emojiReactionsByChatID"
                       :params     [chat-id cursor constants/default-number-of-messages]
                       :on-success #(rf/dispatch [:reactions/loaded chat-id session-id (map <-rpc %)])
                       :on-error   #(log/error "failed loading reactions" chat-id %)}]})))

(rf/reg-event-fx :reactions/loaded
 (fn [{db :db} [chat-id session-id reactions]]
   (when-not (and (get-in db [:pagination-info chat-id :messages-initialized?])
                  (not= session-id
                        (get-in db [:pagination-info chat-id :messages-initialized?])))
     (let [reactions-w-chat-id (map #(assoc % :chat-id chat-id) reactions)]
       {:db (update db :reactions (process-reactions (:chats db)) reactions-w-chat-id)}))))

(defn- format-authors-response
  [response]
  (->> response
       (map (fn [item]
              {:compressed-key (:compressedKey item)
               :emoji-id       (:emojiId item)
               :from           (:from item)}))
       (group-by :emoji-id)))

(rf/reg-event-fx :reactions/get-authors-by-message-id
 (fn [{:keys [db]} [{:keys [message-id on-success]}]]
   {:db            (dissoc db :reactions/authors)
    :json-rpc/call [{:method     "wakuext_emojiReactionsByChatIDMessageID"
                     :params     [(:current-chat-id db) message-id]
                     :on-error   #(log/error "failed to fetch emoji reaction by message-id: "
                                             {:message-id message-id :error %})
                     :on-success #(when on-success
                                    (on-success (format-authors-response %)))}]}))

(rf/reg-event-fx :reactions/save-authors
 (fn [{:keys [db]} [reaction-authors]]
   {:db (assoc db :reactions/authors reaction-authors)}))

(rf/reg-event-fx :reactions/clear-authors
 (fn [{:keys [db]}]
   {:db (dissoc db :reactions/authors)}))

(rf/reg-event-fx :reactions/send-emoji-reaction
 (fn [{{:keys [current-chat-id]} :db} [{:keys [message-id emoji-id]}]]
   {:json-rpc/call [{:method      "wakuext_sendEmojiReaction"
                     :params      [current-chat-id message-id emoji-id]
                     :js-response true
                     :on-success  #(rf/dispatch [:sanitize-messages-and-process-response %])
                     :on-error    #(log/error "failed to send a reaction" %)}]}))

(rf/reg-event-fx :reactions/send-emoji-reaction-retraction
 (fn [_ [emoji-reaction-id]]
   {:json-rpc/call [{:method      "wakuext_sendEmojiReactionRetraction"
                     :params      [emoji-reaction-id]
                     :js-response true
                     :on-success  #(rf/dispatch [:sanitize-messages-and-process-response %])
                     :on-error    #(log/error "failed to send a reaction retraction" %)}]}))
