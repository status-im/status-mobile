(ns status-im.chat.events.send-message
  (:require [taoensso.timbre :as log]
            [re-frame.core :as re-frame]
            [status-im.chat.models.message :as message-model]
            [status-im.native-module.core :as status]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.types :as types]))

(re-frame/reg-fx
 :send-notification
 (fn [{:keys [message payload tokens]}]
   (let [payload-json (types/clj->json payload)
         tokens-json  (types/clj->json tokens)]
     (log/debug "send-notification message: " message " payload-json: " payload-json " tokens-json: " tokens-json)
     (status/notify-users {:message message :payload payload-json :tokens tokens-json} #(log/debug "send-notification cb result: " %)))))
