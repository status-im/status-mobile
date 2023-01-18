(ns status-im.chat.db
  (:require [status-im2.setup.constants :as constants]))

(defn group-chat-name
  [{:keys [public? name]}]
  (str (when public? "#") name))

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
