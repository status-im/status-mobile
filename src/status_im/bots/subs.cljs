(ns status-im.bots.subs
  (:require [re-frame.core :as re-frame]
            [status-im.chat.models.input :as input-model]))

(re-frame/reg-sub
  :current-bot-db
  (fn [db]
    (let [current-chat-id (re-frame/subscribe [:get-current-chat-id])
          command-owner (-> db
                            (input-model/selected-chat-command @current-chat-id)
                            :command
                            :owner-id)]
      [command-owner (get-in db [:bot-db command-owner])])))
