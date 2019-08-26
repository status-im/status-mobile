(ns status-im.notifications.background
  (:require [goog.object :as object]
            [re-frame.core :as re-frame]
            [status-im.react-native.js-dependencies :as rn]
            [status-im.notifications.core :as notifications]
            [status-im.i18n :as i18n]
            [cljs.core.async :as async]
            [taoensso.timbre :as log]
            [status-im.utils.platform :as platform]))

(when-not platform/desktop?
  (def firebase (object/get rn/react-native-firebase "default")))

(defn message-handler-fn []
  ;; message-js is firebase.messaging.RemoteMessage: https://github.com/invertase/react-native-firebase-docs/blob/master/docs/messaging/reference/RemoteMessage.md
  (fn [message-js]
    (js/Promise.
     (fn [on-success on-error]
       (try
         (when message-js
           (log/debug "message-handler-fn called" (js/JSON.stringify message-js))
           (let [decoded-payload (notifications/decode-notification-payload message-js)]
             (when decoded-payload
               (log/debug "dispatching :notifications.callback/on-message to display background message" decoded-payload)
               (re-frame/dispatch [:notifications.callback/on-message decoded-payload {:force true}]))))
         (on-success)
         (catch :default e
           (log/warn "failed to handle background message" e)
           (on-error e)))))))
