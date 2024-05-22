(ns status-im.subs.messages
  (:require
    [re-frame.core :as re-frame]
    [status-im.constants :as constants]
    [status-im.contexts.chat.messenger.messages.list.events :as models.message-list]
    [status-im.contexts.chat.messenger.messages.resolver.message-resolver :as resolver]
    [utils.i18n :as i18n]))

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
    (empty? acc)                                            ; initial element
    {:last-timestamp whisper-timestamp
     :last-datemark  datemark
     :acc            (conj acc msg)}

    (and (not= last-datemark datemark)                      ; not the same day
         (< whisper-timestamp last-timestamp))              ; not out-of-order
    {:last-timestamp whisper-timestamp
     :last-datemark  datemark
     :acc            (conj acc
                           {:value last-datemark            ; intersperse datemark message
                            :type  :datemark}
                           msg)}
    :else
    {:last-timestamp (min whisper-timestamp last-timestamp) ; use last datemark
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
  (->> messages
       (reduce
        (fn [{:keys [messages albums]} message]
          (let [{:keys [album-id content quoted-message]} message
                {:keys [response-to]}                     content
                albums                                    (cond-> albums
                                                            album-id
                                                            (update album-id conj message))
                messages                                  (if album-id
                                                            (conj (filterv #(not= album-id (:album-id %))
                                                                           messages)
                                                                  (merge message
                                                                         {:album (get albums album-id)
                                                                          :album-id album-id
                                                                          :content {:response-to
                                                                                    response-to}
                                                                          :quoted-message quoted-message
                                                                          :content-type
                                                                          constants/content-type-album}))
                                                            (conj messages message))]
            {:messages messages
             :albums   albums}))
        {:messages []
         :albums   {}})
       :messages))

(re-frame/reg-sub
 :chats/chat-messages
 :<- [:messages/messages]
 (fn [messages [_ chat-id]]
   (get messages chat-id {})))

(re-frame/reg-sub
 :chats/message-link-previews
 :<- [:messages/messages]
 (fn [messages [_ chat-id message-id]]
   (get-in messages [chat-id message-id :link-previews])))

(re-frame/reg-sub
 :chats/message-link-previews?
 :<- [:messages/messages]
 (fn [messages [_ chat-id message-id]]
   (-> messages
       (get-in [chat-id message-id :link-previews])
       count
       pos?)))

(re-frame/reg-sub
 :chats/message-status-link-previews
 :<- [:messages/messages]
 (fn [messages [_ chat-id message-id]]
   (get-in messages [chat-id message-id :status-link-previews])))

(re-frame/reg-sub
 :chats/pinned
 :<- [:messages/pin-messages]
 (fn [pinned-messages [_ chat-id] _]
   (get pinned-messages chat-id)))

;; local messages will not have a :pinned-at key until user navigates away and to
;; chat screen. For this reason we want to retain order of local messages with :pinned-at nil
;; as these will be a stack on top of the messages, however we do want to sort previous messages
;; from backend that have a :pinned-at value.
(defn sort-pinned
  [a b]
  (cond
    (and a b) (- a b)
    (or a b)  (if b false true)
    :else     a))

(re-frame/reg-sub
 :chats/pinned-sorted-list
 (fn [[_ chat-id] _]
   (re-frame/subscribe [:chats/pinned chat-id]))
 (fn [pin-messages _]
   (let [pin-messages-vals (vals pin-messages)]
     (sort-by :pinned-at sort-pinned pin-messages-vals))))

(re-frame/reg-sub
 :chats/last-pinned-message
 (fn [[_ chat-id]]
   (re-frame/subscribe [:chats/pinned-sorted-list chat-id]))
 (fn [pin-messages _]
   (last pin-messages)))

(defn message-text
  [{:keys [content-type] :as message}]
  (cond (= content-type constants/content-type-audio)
        (i18n/label :t/audio-message)
        :else
        (get-in message [:content :parsed-text])))

(re-frame/reg-sub
 :chats/last-pinned-message-text
 (fn [[_ chat-id]]
   (re-frame/subscribe [:chats/last-pinned-message chat-id]))
 (fn [pinned-message _]
   (let [latest-pin-text                    (message-text pinned-message)
         {:keys [deleted? deleted-for-me?]} pinned-message]
     (cond deleted?                  (i18n/label :t/message-deleted-for-everyone)
           deleted-for-me?           (i18n/label :t/message-deleted-for-you)
           (string? latest-pin-text) latest-pin-text
           :else                     (resolver/resolve-message latest-pin-text)))))

(re-frame/reg-sub
 :chats/pin-messages-count
 (fn [[_ chat-id]]
   (re-frame/subscribe [:chats/pinned chat-id]))
 (fn [pinned-messages _]
   (count pinned-messages)))

(re-frame/reg-sub
 :chats/message-reactions
 :<- [:multiaccount/public-key]
 :<- [:messages/reactions]
 (fn [[current-public-key reactions] [_ message-id chat-id]]
   (let [reactions (get-in reactions [chat-id message-id])]
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
      reactions))))

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
 :chats/message-list
 :<- [:messages/message-lists]
 (fn [message-lists [_ chat-id]]
   (get message-lists chat-id)))

(re-frame/reg-sub
 :chats/raw-chat-messages-stream
 (fn [[_ chat-id] _]
   [(re-frame/subscribe [:chats/message-list chat-id])
    (re-frame/subscribe [:chats/chat-messages chat-id])
    (re-frame/subscribe [:chats/pinned chat-id])
    (re-frame/subscribe [:chats/loading-messages? chat-id])])
 (fn [[message-list messages pin-messages loading-messages?] _]
   ;;TODO (perf)
   (let [message-list-seq (models.message-list/->seq message-list)]
     ; Don't show gaps if that's the case as we are still loading messages
     (if (and (empty? message-list-seq) loading-messages?)
       []
       (-> message-list-seq
           (add-datemarks)
           (hydrate-messages messages pin-messages)
           (albumize-messages))))))

(re-frame/reg-sub
 :messages/resolve-mention
 (fn [[_ mention] _]
   [(re-frame/subscribe [:contacts/contact-two-names-by-identity mention])])
 (fn [[contact-names] [_ mention]]
   (if (= mention constants/everyone-mention-id)
     (i18n/label :t/everyone-mention)
     (first contact-names))))
