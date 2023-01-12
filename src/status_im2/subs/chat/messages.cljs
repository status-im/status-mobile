(ns status-im2.subs.chat.messages
  (:require [re-frame.core :as re-frame]
            [status-im2.contexts.chat.messages.list.events :as models.message-list]
            [status-im.chat.models.reactions :as models.reactions]
            [utils.datetime :as datetime]
            [status-im2.constants :as constants]))

(defn intersperse-datemark
  "Reduce step which expects the input list of messages to be sorted by clock value.
  It makes best effort to group them by day.
  We cannot sort them by :timestamp, as that represents the clock of the sender
  and we have no guarantees on the order.
  We naively and arbitrarly group them assuming that out-of-order timestamps
  fall in the previous bucket.
  A sends M1 to B with timestamp 2000-01-01T00:00:00
  B replies M2 with timestamp    1999-12-31-23:59:59
  M1 needs to be displayed before M2
  so we bucket both in 1999-12-31"
  [{:keys [acc last-timestamp last-datemark]} {:keys [whisper-timestamp datemark] :as msg}]
  (cond
    (empty? acc)                       ; initial element
    {:last-timestamp whisper-timestamp
     :last-datemark  datemark
     :acc            (conj acc msg)}

    (and (not= last-datemark datemark) ; not the same day
         (< whisper-timestamp last-timestamp))               ; not out-of-order
    {:last-timestamp whisper-timestamp
     :last-datemark  datemark
     :acc            (conj acc
                           {:value last-datemark ; intersperse datemark message
                            :type  :datemark}
                           msg)}
    :else
    {:last-timestamp (min whisper-timestamp last-timestamp)  ; use last datemark
     :last-datemark  last-datemark
     :acc            (conj acc (assoc msg :datemark last-datemark))}))

(defn add-datemarks
  "Add a datemark in between an ordered seq of messages when two datemarks are not
  the same. Ignore messages with out-of-order timestamps"
  [messages]
  (when (seq messages)
    (let [messages-with-datemarks (:acc (reduce intersperse-datemark {:acc []} messages))]
      ; Append last datemark
      (conj messages-with-datemarks
            {:value (:datemark (peek messages-with-datemarks))
             :type  :datemark}))))

(defn last-gap
  "last-gap is a special gap that is put last in the message stream"
  [chat-id synced-from]
  {:message-id     "0x123"
   :message-type   constants/message-type-gap
   :chat-id        chat-id
   :content-type   constants/content-type-gap
   :gap-ids        #{:first-gap}
   :gap-parameters {:from synced-from}})

(defn collapse-gaps
  "collapse-gaps will take an array of messages and collapse any gap next to
  each other in a single gap.
  It will also append one last gap if the last message is a non-gap"
  [messages chat-id synced-from now chat-type joined loading-messages?]
  (let [messages-with-gaps (reduce
                            (fn [acc {:keys [gap-parameters message-id] :as message}]
                              (let [last-element (peek acc)]
                                (cond
                                  ;; If it's a message, just add
                                  (empty? gap-parameters)
                                  (conj acc message)

                                  ;; Both are gaps, merge them
                                  (and
                                   (seq (:gap-parameters last-element))
                                   (seq gap-parameters))
                                  (conj (pop acc) (update last-element :gap-ids conj message-id))

                                  ;; it's a gap
                                  :else
                                  (conj acc (assoc message :gap-ids #{message-id})))))
                            []
                            messages)]
    (if (or loading-messages? ; it's loading messages from the database
            (nil? synced-from) ; it's still syncing
            (= constants/timeline-chat-type chat-type) ; it's a timeline chat
            (= constants/profile-chat-type chat-type) ; it's a profile chat
            (and (not (nil? synced-from)) ; it's not more than a month
                 (<= synced-from (- (quot now 1000) constants/one-month)))
            (and (= constants/private-group-chat-type chat-type) ; it's a private group chat
                 (or (not (pos? joined)) ; we haven't joined
                     (>= (quot joined 1000) synced-from))) ; the history goes before we joined
            (:gap-ids (peek messages-with-gaps))) ; there's already a gap on top of the chat history
      messages-with-gaps ; don't add an extra gap
      (conj messages-with-gaps (last-gap chat-id synced-from)))))

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

(defn albumize-messages
  [messages]
  (get (reduce (fn [{:keys [messages albums]} message]
                 (let [album-id (when (:albumize? message) (:album-id message))
                       albums   (cond-> albums album-id (update album-id conj message))
                       messages (if (and album-id (> (count (get albums album-id)) 3))
                                  (conj (filterv #(not= album-id (:album-id %)) messages)
                                        {:album        (get albums album-id)
                                         :album-id     album-id
                                         :message-id   album-id
                                         :content-type constants/content-type-album})
                                  (conj messages message))]
                   {:messages messages
                    :albums   albums}))
               {:messages []
                :albums   {}}
               messages)
       :messages))

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

(re-frame/reg-sub
 :chats/chat-no-messages?
 (fn [[_ chat-id] _]
   (re-frame/subscribe [:chats/chat-messages chat-id]))
 (fn [messages]
   (empty? messages)))

<<<<<<< HEAD
=======
(defn albumize-messages
  [messages]
  (get (reduce (fn [{:keys [messages albums]} message]
                 (let [album-id (when (:albumize? message) (:album-id message))
                       albums   (cond-> albums album-id (update album-id conj message))
                       messages (if (and album-id (> (count (get albums album-id)) 1))
                                  (conj (filterv #(not= album-id (:album-id %)) messages)
                                        {:album        (get albums album-id)
                                         :album-id     album-id
                                         :message-id   album-id
                                         :content-type constants/content-type-album})
                                  (conj messages message))]
                   {:messages messages
                    :albums   albums}))
               {:messages []
                :albums   {}}
               messages)
       :messages))

>>>>>>> 7ab466fd8... updates
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
           (add-datemarks)
           (hydrate-messages messages pin-messages)
           (collapse-gaps chat-id
                          synced-from
                          (datetime/timestamp)
                          chat-type
                          joined
                          loading-messages?)
           (albumize-messages))))))
