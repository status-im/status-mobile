(ns status-im.chat.events.requests
  (:require    [status-im.constants :as constants]))

;; Functions

(defn request-answered
  "Takes chat-id, message-id and cofx, returns fx necessary data for marking request as answered"
  [chat-id message-id {:keys [db]}]
  (when message-id
    {:db                                  (update-in db [:chats chat-id :requests] dissoc message-id)
     :data-store/mark-request-as-answered {:chat-id    chat-id
                                           :message-id message-id}}))

(defn add-request
  "Takes chat-id, message-id + cofx and returns fx with necessary data for adding new request"
  [chat-id message-id {:keys [db]}]
  (let [{:keys [content-type content]} (get-in db [:chats chat-id :messages message-id])]
    (when (= content-type constants/content-type-command-request)
      (let [request {:chat-id    chat-id
                     :message-id message-id
                     :response   (:request-command content)
                     :status     "open"}]
        {:db                      (assoc-in db [:chats chat-id :requests message-id] request)
         :data-store/save-request request}))))
