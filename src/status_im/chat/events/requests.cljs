(ns status-im.chat.events.requests
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.data-store.requests :as requests-store]))

;; Coeffects

(re-frame/reg-cofx
  :get-stored-unanswered-requests
  (fn [cofx _]
    (assoc cofx :stored-unanswered-requests (requests-store/get-all-unanswered))))

;; Effects
(re-frame/reg-fx
 :chat-requests/mark-as-answered
 (fn [{:keys [chat-id message-id]}]
   (requests-store/mark-as-answered chat-id message-id)))

(re-frame/reg-fx
  ::save-request
  (fn [request]
    (requests-store/save request)))

;; Handlers

(handlers/register-handler-fx
 :request-answered
 [re-frame/trim-v]
 (fn [{:keys [db]} [chat-id message-id]]
   {:db (update-in db [:chats chat-id :requests] dissoc message-id)
    :chat-requests/mark-as-answered {:chat-id  chat-id
                                     :message-id message-id}}))

(defn add-request
  "Takes fx, chat-id and message, updates fx with necessary data for adding new request"
  [fx chat-id {:keys [message-id content]}]
  (let [request {:chat-id    chat-id
                 :message-id message-id
                 :response   (:command content)
                 :status     "open"}]
    (-> fx
        (assoc-in [:db :chats chat-id :requests message-id] request)
        (assoc ::save-request request))))
