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

(defn check-author-direction
  [db chat-id {:keys [from outgoing] :as message}]
  (let [previous-message (first (get-in db [:chats chat-id :messages]))]
    (merge message
           {:same-author    (if previous-message
                              (= (:from previous-message) from)
                              true)
            :same-direction (if previous-message
                              (= (:outgoing previous-message) outgoing)
                              true)})))
