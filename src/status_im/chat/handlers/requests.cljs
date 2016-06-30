(ns status-im.chat.handlers.requests
  (:require [re-frame.core :refer [after]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.persistence.realm :as realm]))

(defn store-request!
  [{:keys [new-request]}]
  (realm/write
    (fn []
      (realm/create :requests new-request))))

(defn add-request
  [db [_ chat-id {:keys [msg-id content]}]]
  (let [request {:chat-id    chat-id
                 :message-id msg-id
                 :type       (:command content)
                 :added      (js/Date.)}
        request' (update request :type keyword)]
    (-> db
        (update-in [:chats chat-id :requests] conj request')
        (assoc :new-request request))))

(defn load-requests!
  [{:keys [current-chat-id] :as db} [_ chat-id]]
  (let [chat-id' (or chat-id current-chat-id)
        requests (-> :requests
                     ;; todo maybe limit is needed
                     (realm/get-by-fieds {:chat-id chat-id'
                                          :status  "open"})
                     (realm/sorted :added :desc)
                     (realm/collection->map))
        requests' (map #(update % :type keyword) requests)]
    (assoc-in db [:chats chat-id' :requests] requests')))

(register-handler :add-request
  (after store-request!)
  add-request)

(register-handler :load-requests! load-requests!)
