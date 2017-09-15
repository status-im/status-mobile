(ns status-im.commands.events.jail
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.data-store.handler-data :as handler-data]
            [taoensso.timbre :as log]))

(re-frame/reg-fx
  :save-handler-data
  (fn [data]
    (handler-data/save-data data)))

(handlers/register-handler-fx
  :set-handler-data
  [re-frame/trim-v]
  (fn [{:keys [db]} [chat-id {:keys [messageId] :as data}]]
    (let [;; this is very bad, we should refactor our db ASAP
          message          (->> (get-in db [:chats chat-id :messages])
                                (filter #(= (:message-id %) messageId))
                                first)
          handler-data     (cond-> (dissoc data :messageId)
                             ;; message not there yet, indicate we want to re-fetch preview once it lands there
                             (nil? message)
                             (assoc :fetch-preview true))
          old-handler-data (get-in db [:handler-data messageId] {})
          new-handler-data (merge old-handler-data handler-data)]
      (cond-> {:db (assoc-in db [:handler-data messageId] new-handler-data)
               :save-handler-data {:message-id messageId
                                   :data new-handler-data}}
        ;; message was already added to db, we can re-fetch preview
        (not (nil? message))
        (assoc :dispatch [:request-command-data (assoc message :jail-id chat-id) :preview])))))
