(ns status-im.bots.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
  :bot-subscription
  (fn [db [_ path]]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (get-in db (concat [:bot-db @chat-id] path)))))

(reg-sub
  :current-bot-db
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (get-in db [:bot-db @chat-id]))))
