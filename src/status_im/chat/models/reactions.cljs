(ns status-im.chat.models.reactions
  (:require
    [re-frame.core :as re-frame]
    [status-im.data-store.reactions :as data-store.reactions]
    [status-im.transport.message.protocol :as message.protocol]
    [status-im2.constants :as constants]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]
    [utils.transforms :as transforms]))

(defn update-reaction
  [acc retracted chat-id message-id emoji-id emoji-reaction-id reaction]
  ;; NOTE(Ferossgp): For a better performance, better to not keep in db all retracted reactions
  ;; retraction will always come after the reaction so there shouldn't be a conflict
  (if retracted
    (update-in acc [chat-id message-id emoji-id] dissoc emoji-reaction-id)
    (assoc-in acc [chat-id message-id emoji-id emoji-reaction-id] reaction)))

(defn process-reactions
  [_]
  (fn [reactions new-reactions]
    ;; TODO(Ferossgp): handling own reaction in subscription could be expensive,
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

(rf/defn load-more-reactions
  {:events [:load-more-reactions]}
  [{:keys [db]} cursor chat-id]
  (when-let [session-id (get-in db [:pagination-info chat-id :messages-initialized?])]
    (data-store.reactions/reactions-by-chat-id-rpc
     chat-id
     cursor
     constants/default-number-of-messages
     #(re-frame/dispatch [::reactions-loaded chat-id session-id %])
     #(log/error "failed loading reactions" chat-id %))))

(rf/defn reactions-loaded
  {:events [::reactions-loaded]}
  [{db :db}
   chat-id
   session-id
   reactions]
  (when-not (and (get-in db [:pagination-info chat-id :messages-initialized?])
                 (not= session-id
                       (get-in db [:pagination-info chat-id :messages-initialized?])))
    (let [reactions-w-chat-id (map #(assoc % :chat-id chat-id) reactions)]
      {:db (update db :reactions (process-reactions (:chats db)) reactions-w-chat-id)})))


;; Send reactions


(rf/defn send-emoji-reaction
  {:events [:models.reactions/send-emoji-reaction]}
  [{{:keys [current-chat-id]} :db :as cofx} reaction]
  (message.protocol/send-reaction cofx
                                  (update reaction :chat-id #(or % current-chat-id))))

(rf/defn send-retract-emoji-reaction
  {:events [:models.reactions/send-emoji-reaction-retraction]}
  [{{:keys [current-chat-id]} :db :as cofx} reaction]
  (message.protocol/send-retract-reaction cofx
                                          (update reaction :chat-id #(or % current-chat-id))))


(defn message-reactions
  [current-public-key reactions]
  (reduce
   (fn [acc [emoji-id reactions]]
     (if (pos? (count reactions))
       (let [own (first (filter (fn [[_ {:keys [from]}]]
                                  (= from current-public-key))
                                reactions))]
         (conj acc
               {:emoji-id          emoji-id
                :own               (boolean (seq own))
                :emoji-reaction-id (:emoji-reaction-id (second own))
                :quantity          (count reactions)}))
       acc))
   []
   reactions))

(defn- <-rpc
  [{compressed-key :compressedKey
    emoji-id       :emojiId
    :keys          [from]}]
  {:compressed-key compressed-key
   :emoji-id       emoji-id
   :from           from})

(defn- format-response
  [response]
  (->> (transforms/js->clj response)
       (map <-rpc)
       (group-by :emoji-id)))

(rf/defn save-emoji-reaction-details
  {:events [:chat/save-emoji-reaction-details]}
  [{:keys [db]} reaction-authors]
  {:db (assoc db :chat/reactions-authors reaction-authors)})

(rf/defn clear-emoji-reaction-details
  {:events [:chat/clear-emoji-reaction-author-details]}
  [{:keys [db]}]
  {:db (dissoc db :chat/reactions-authors)})

(rf/defn emoji-reactions-by-message-id
  {:events [:chat.ui/emoji-reactions-by-message-id]}
  [{:keys [db]} {:keys [message-id on-success]}]
  {:db            (dissoc db :chat/reactions-authors)
   :json-rpc/call [{:method      "wakuext_emojiReactionsByChatIDMessageID"
                    :params      [(:current-chat-id db) message-id]
                    :js-response true
                    :on-error    #(log/error "failed to fetch emoji reaction by message-id: "
                                             {:message-id message-id :error %})
                    :on-success  #(when on-success
                                    (on-success (format-response %)))}]})
