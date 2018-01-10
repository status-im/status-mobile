(ns status-im.chat.events.send-message
  (:require [re-frame.core :as re-frame]
            [status-im.chat.utils :as chat-utils]
            [status-im.chat.models.message :as message-model]
            [status-im.constants :as constants]
            [status-im.data-store.chats :as chats-store]
            [status-im.native-module.core :as status]
            [status-im.protocol.core :as protocol]
            [status-im.utils.config :as config]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.random :as random]
            [status-im.utils.types :as types]
            [status-im.utils.datetime :as datetime]
            [taoensso.timbre :as log]))

(re-frame/reg-fx
  :send-notification
  (fn [fcm-token]
    (log/debug "send-notification fcm-token: " fcm-token)
    (status/notify fcm-token #(log/debug "send-notification cb result: " %))))

(re-frame/reg-fx
  :send-group-message
  (fn [value]
    (protocol/send-group-message! value)))

(re-frame/reg-fx
  :send-public-group-message
  (fn [value]
    (protocol/send-public-group-message! value)))

(re-frame/reg-fx
  :send-message
  (fn [value]
   (protocol/send-message! value)))

(re-frame/reg-fx
  :update-message-overhead!
  (fn [[chat-id network-status]]
    (if (= network-status :offline)
      (chats-store/inc-message-overhead chat-id)
      (chats-store/reset-message-overhead chat-id))))

;;;; Handlers

(handlers/register-handler-fx
  :chat-send-message/send-command
  message-model/send-interceptors
  (fn [cofx [add-to-chat-id params]]
    (message-model/send-command cofx nil add-to-chat-id params)))

(handlers/register-handler-fx
  :chat-send-message/from-jail
  [re-frame/trim-v]
  (fn [cofx [{:keys [chat-id message]}]]
    (let [parsed-message (types/json->clj message)]
      (message-model/handle-message-from-bot cofx {:message parsed-message
                                                   :chat-id chat-id}))))
