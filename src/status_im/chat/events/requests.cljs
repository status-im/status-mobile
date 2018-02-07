(ns status-im.chat.events.requests
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.utils.handlers :as handlers]
            [status-im.data-store.requests :as requests-store]))

;; Coeffects

(re-frame/reg-cofx
  :get-stored-unanswered-requests
  (fn [cofx _]
    (assoc cofx :stored-unanswered-requests (requests-store/get-all-unanswered))))

;; Effects
(re-frame/reg-fx
  ::mark-as-answered
  (fn [{:keys [chat-id message-id]}]
    (requests-store/mark-as-answered chat-id message-id)))

(re-frame/reg-fx
  ::save-request
  (fn [request]
    (requests-store/save request)))

;; Functions

(defn request-answered
  "Takes chat-id, message-id and cofx, returns fx necessary data for marking request as answered"
  [chat-id message-id {:keys [db]}]
  (when message-id
    {:db                (update-in db [:chats chat-id :requests] dissoc message-id)
     ::mark-as-answered {:chat-id    chat-id
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
        {:db            (assoc-in db [:chats chat-id :requests message-id] request)
         ::save-request request}))))
