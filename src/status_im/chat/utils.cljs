(ns status-im.chat.utils)

(defn console? [s]
  (= "console" s))

(def not-console?
  (complement console?))

(defn add-message-to-db
  ([db chat-id message] (add-message-to-db db chat-id message true))
  ([db chat-id message new?]
   (let [messages [:chats chat-id :messages]]
     (update-in db messages conj (assoc message :chat-id chat-id
                                                :new? (if (nil? new?)
                                                        true
                                                        new?))))))

(defn- check-message [previous-message {:keys [from outgoing] :as message}]
  (merge message
         {:same-author    (if previous-message
                            (= (:from previous-message) from)
                            false)
          :same-direction (if previous-message
                            (= (:outgoing previous-message) outgoing)
                            true)}))

(defn check-author-direction
  ([previous-message message]
   (check-message previous-message message))
  ([db chat-id message]
   (let [previous-message (first (get-in db [:chats chat-id :messages]))]
     (check-message previous-message message))))
