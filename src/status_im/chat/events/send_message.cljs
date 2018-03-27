(ns status-im.chat.events.send-message
  (:require [taoensso.timbre :as log]
            [re-frame.core :as re-frame]
            [status-im.chat.models.message :as message-model]
            [status-im.native-module.core :as status]
            [status-im.protocol.core :as protocol] 
            [status-im.utils.handlers :as handlers] 
            [status-im.utils.types :as types]))

(re-frame/reg-fx
  :send-notification
  (fn [{:keys [message payload tokens]}]
    (let [payload-json (types/clj->json payload)
          tokens-json  (types/clj->json tokens)]
      (log/debug "send-notification message: " message " payload-json: " payload-json " tokens-json: " tokens-json)
      (status/notify-users {:message message :payload payload-json :tokens tokens-json} #(log/debug "send-notification cb result: " %)))))

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

;;;; Handlers

(handlers/register-handler-fx
  :chat-send-message/send-command
  message-model/send-interceptors
  (fn [cofx [_ params]]
    (message-model/send-command cofx params)))

(handlers/register-handler-fx
  :chat-send-message/from-jail
  [re-frame/trim-v]
  (fn [cofx [{:keys [chat-id message]}]]
    (let [parsed-message (types/json->clj message)]
      (message-model/handle-message-from-bot cofx {:message parsed-message
                                                   :chat-id chat-id}))))
