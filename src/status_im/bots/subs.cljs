(ns status-im.bots.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]))

(re-frame/register-sub
  :bot-subscription
  (fn [db [_ path]]
    (let [chat-id (re-frame/subscribe [:get-current-chat-id])]
      (reaction (get-in @db (concat [:bot-db @chat-id] path))))))

(re-frame/register-sub
  :current-bot-db
  (fn [db]
    (let [chat-id (re-frame/subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:bot-db @chat-id])))))
