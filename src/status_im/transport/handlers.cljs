(ns ^{:doc "Events for message handling"}
    status-im.transport.handlers
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.transport.message.core :as message]
            [status-im.transport.core :as transport]
            [status-im.chat.models :as models.chat]
            [status-im.utils.datetime :as datetime]
            [taoensso.timbre :as log]
            [status-im.transport.utils :as transport.utils]
            [cljs.reader :as reader]
            [status-im.transport.message.transit :as transit]
            [status-im.transport.shh :as shh]
            [status-im.transport.filters :as filters]
            [status-im.utils.utils :as utils]
            [status-im.i18n :as i18n]))

(handlers/register-handler-fx
  :protocol/receive-whisper-message
  [re-frame/trim-v (re-frame/inject-cofx :random-id)]
  (fn [cofx [js-error js-message chat-id]]
    (let [{:keys [payload sig]} (js->clj js-message :keywordize-keys true)
          status-message        (-> payload
                                    transport.utils/to-utf8
                                    transit/deserialize)]
      (if (and sig status-message)
        (if (transit/unknown-message-type? status-message)
          (do (log/error :incompatible-message-received status-message)
              (utils/show-popup (i18n/label :t/error)
                                (i18n/label :t/incompatible-message))))
        (message/receive status-message (or chat-id sig) sig cofx)))))

(handlers/register-handler-fx
  :protocol/send-status-message-success
  [re-frame/trim-v]
  (fn [{:keys [db] :as cofx} [_ resp]]
    (log/debug :send-status-message-success resp)))

(handlers/register-handler-fx
  :protocol/send-status-message-error
  [re-frame/trim-v]
  (fn [{:keys [db] :as cofx} [err]]
    (log/error :send-status-message-error err)))
