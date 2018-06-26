(ns status-im.bots.subs
  (:require [re-frame.core :as re-frame]
            [status-im.chat.models.input :as input-model]))

(re-frame/reg-sub :get-bot-db :bot-db)

(re-frame/reg-sub
 :current-bot-db
 :<- [:get-bot-db]
 :<- [:selected-chat-command]
 (fn [[bot-db command]]
   (let [command-owner (get-in command [:command :owner-id])]
     [command-owner (get bot-db command-owner)])))
