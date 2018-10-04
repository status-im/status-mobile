(ns status-im.ui.screens.desktop.main.buidl.core
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.chat.models.message :as message]
            [status-im.utils.fx :as fx]))

(re-frame/reg-sub
 :buidl/get-messages
 (fn [db]
   (vals (get-in db [:chats "status-buidl-test" :messages]))))

(re-frame/reg-sub
 :buidl/get-tags
 :<- [:buidl/get-messages]
 (fn [messages]
   (reduce (fn [acc {:keys [content]}]
             (reduce (fn [acc tag]
                       (update acc tag inc))
                     acc
                     (:tags content)))
           {}
           messages)))

(fx/defn send-buidl [cofx content]
  (message/send-message cofx {:chat-id      "status-buidl-test"
                              :content-type "buidl"
                              :content content}))

(handlers/register-handler-fx
 :send-buidl-message
 (fn [cofx [_ buidl-message]]
   (send-buidl cofx buidl-message)))
