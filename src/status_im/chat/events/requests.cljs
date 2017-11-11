(ns status-im.chat.events.requests
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.requests :as requests]
            [status-im.utils.handlers :as handlers]
            [taoensso.timbre :as log]))

;;;; Effects

(re-frame/reg-fx
  :save-requests
  (fn [requests]
    (requests/save-all requests)))

(re-frame/reg-fx
  ::mask-as-answered
  (fn [[chat-id message-id]]
    (requests/mark-as-answered chat-id message-id)))

;;;; Helper fns

(defn add-request
  [{:keys [db save-entities]} chat-id {:keys [message-id content]}]
  (let [request  {:chat-id    chat-id
                  :message-id message-id
                  :bot        (:bot content)
                  :type       (:command content)
                  :added      (js/Date.)}
        request' (update request :type keyword)]
    {:db            (update-in db [:chats chat-id :requests] conj request')
     :save-entities (conj (or save-entities []) [:request request])}))

(defn update-db-with-events [db chat-id]
  (let [;; TODO: maybe limit is needed
        requests (map #(update % :type keyword)
                   (requests/get-available-by-chat-id chat-id))]
    (assoc-in db [:chats chat-id :requests] requests)))

;;;; Handlers

(handlers/register-handler-fx
  :chat-requests/add
  (fn [fx [_ chat-id request]]
    (add-request fx chat-id request)))

(handlers/register-handler-db
  :chat-requests/load
  (fn [{:keys [current-chat-id] :as db} [_ chat-id]]
    (update-db-with-events db (or chat-id current-chat-id))))

(handlers/register-handler-fx
  :chat-requests/mark-as-answered
  (fn [{:keys [db]} [_ chat-id message-id]]
    {::mask-as-answered [chat-id message-id]
     :db                (update-db-with-events db chat-id)}))