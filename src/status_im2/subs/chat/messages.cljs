(ns status-im2.subs.chat.messages
  (:require [re-frame.core :as re-frame]
            [status-im.chat.db :as chat.db]
            [status-im.chat.models.message-list :as models.message-list]
            [status-im.chat.models.reactions :as models.reactions]
<<<<<<< HEAD
            [utils.datetime :as datetime]
            [status-im2.common.constants :as constants]))
=======
            [status-im.utils.datetime :as datetime]
            [status-im2.common.constants :as constants]
            [status-im.constants]))
>>>>>>> 0424ac6e4... feat: images album

(re-frame/reg-sub
  :chats/chat-messages
  :<- [:messages/messages]
  (fn [messages [_ chat-id]]
    (get messages chat-id {})))

(re-frame/reg-sub
  :chats/pinned
  (fn [[_ chat-id] _]
    [(re-frame/subscribe [:messages/pin-messages])
     (re-frame/subscribe [:chats/chat-messages chat-id])])
  (fn [[pin-messages messages] [_ chat-id]]
    (let [pin-messages (get pin-messages chat-id {})]
      (reduce-kv (fn [acc message-id message]
                   (let [{:keys [deleted? deleted-for-me?]} (get messages message-id)]
                     (if (or deleted? deleted-for-me?)
                       acc
                       (assoc acc message-id message))))
                 {}
                 pin-messages))))

(re-frame/reg-sub
  :chats/pinned-sorted-list
  (fn [[_ chat-id] _]
    (re-frame/subscribe [:chats/pinned chat-id]))
  (fn [pin-messages _]
    (->> pin-messages
         vals
         (sort-by :pinned-at <))))

(re-frame/reg-sub
  :chats/pin-modal
  :<- [:messages/pin-modal]
  (fn [pin-modal [_ chat-id]]
    (get pin-modal chat-id)))

(re-frame/reg-sub
  :chats/message-reactions
  :<- [:multiaccount/public-key]
  :<- [:messages/reactions]
  (fn [[current-public-key reactions] [_ message-id chat-id]]
    (models.reactions/message-reactions
      current-public-key
      (get-in reactions [chat-id message-id]))))

(re-frame/reg-sub
  :chats/all-loaded?
  :<- [:messages/pagination-info]
  (fn [pagination-info [_ chat-id]]
    (get-in pagination-info [chat-id :all-loaded?])))

(re-frame/reg-sub
  :chats/loading-messages?
  :<- [:messages/pagination-info]
  (fn [pagination-info [_ chat-id]]
    (get-in pagination-info [chat-id :loading-messages?])))

(re-frame/reg-sub
  :chats/loading-pin-messages?
  :<- [:messages/pagination-info]
  (fn [pagination-info [_ chat-id]]
    (get-in pagination-info [chat-id :loading-pin-messages?])))

(re-frame/reg-sub
  :chats/message-list
  :<- [:messages/message-lists]
  (fn [message-lists [_ chat-id]]
    (get message-lists chat-id)))

(re-frame/reg-sub
  :chats/pin-message-list
  :<- [:messages/pin-message-lists]
  (fn [pin-message-lists [_ chat-id]]
    (get pin-message-lists chat-id)))

(defn hydrate-messages
  "Pull data from messages and add it to the sorted list"
  ([message-list messages] (hydrate-messages message-list messages {}))
  ([message-list messages pinned-messages]
   (keep #(if (= :message (% :type))
            (when-let [message (messages (% :message-id))]
              (let [pinned-message (get pinned-messages (% :message-id))
                    pinned         (if pinned-message true (some? (message :pinned-by)))
                    pinned-by      (when pinned (or (message :pinned-by) (pinned-message :pinned-by)))
                    message        (assoc message :pinned pinned :pinned-by pinned-by)]
                (merge message %)))
            %)
         message-list)))

(re-frame/reg-sub
  :chats/chat-no-messages?
  (fn [[_ chat-id] _]
    (re-frame/subscribe [:chats/chat-messages chat-id]))
  (fn [messages]
    (empty? messages)))

(defn find-albums [messages]
  (let [all-messages (atom [])
        albums       (atom {})]
    (doseq [message messages]
      (let [album-id (when (:albumize message) (:album-id message))]
        (if (and album-id (get @albums album-id))
          (swap! albums update-in [album-id] #(conj % message))
          (swap! albums assoc-in [album-id] [message]))
        (swap! all-messages conj message)
        (when (and album-id (> (count (get @albums album-id)) 3))
          (do
            (swap! all-messages (fn [data] (filterv #(not= album-id (:album-id  %)) data)))
            (swap! all-messages conj {:album      (get @albums album-id)
                                      :album-id     album-id
                                      :message-id   album-id
                                      :content-type status-im.constants/content-type-album})))))
    @all-messages))

(re-frame/reg-sub
  :chats/raw-chat-messages-stream
  (fn [[_ chat-id] _]
    [(re-frame/subscribe [:chats/message-list chat-id])
     (re-frame/subscribe [:chats/chat-messages chat-id])
     (re-frame/subscribe [:chats/pinned chat-id])
     (re-frame/subscribe [:chats/loading-messages? chat-id])
     (re-frame/subscribe [:chats/synced-from chat-id])
     (re-frame/subscribe [:chats/chat-type chat-id])
     (re-frame/subscribe [:chats/joined chat-id])])
  (fn [[message-list messages pin-messages loading-messages? synced-from chat-type joined] [_ chat-id]]
    ;;TODO (perf)
    (let [message-list-seq (models.message-list/->seq message-list)]
      ; Don't show gaps if that's the case as we are still loading messages
      (if (and (empty? message-list-seq) loading-messages?)
        []
        (-> message-list-seq
            (chat.db/add-datemarks)
            (hydrate-messages messages pin-messages)
            (chat.db/collapse-gaps chat-id
                                   synced-from
                                   (datetime/timestamp)
                                   chat-type
                                   joined
                                   loading-messages?)
            (find-albums))))))

;;we want to keep data unchanged so react doesn't change component when we leave screen
(def memo-profile-messages-stream (atom nil))

(re-frame/reg-sub
  :chats/profile-messages-stream
  (fn [[_ chat-id] _]
    [(re-frame/subscribe [:chats/raw-chat-messages-stream chat-id])
     (re-frame/subscribe [:chats/chat-no-messages? chat-id])
     (re-frame/subscribe [:view-id])])
  (fn [[messages empty view-id]]
    (when (or (= view-id :profile) empty)
      (reset! memo-profile-messages-stream messages))
    @memo-profile-messages-stream))

(def memo-timeline-messages-stream (atom nil))

(re-frame/reg-sub
  :chats/timeline-messages-stream
  :<- [:chats/message-list constants/timeline-chat-id]
  :<- [:chats/chat-messages constants/timeline-chat-id]
  :<- [:view-id]
  (fn [[message-list messages view-id]]
    (if (= view-id :status)
      (let [res (-> (models.message-list/->seq message-list)
                    (hydrate-messages messages))]
        (reset! memo-timeline-messages-stream res)
        res)
      @memo-timeline-messages-stream)))
