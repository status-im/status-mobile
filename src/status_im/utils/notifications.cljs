(ns status-im.utils.notifications
  (:require [re-frame.core :as re-frame :refer [dispatch reg-fx]]
            [status-im.utils.handlers :as handlers]
            [status-im.react-native.js-dependencies :as rn]
            [status-im.utils.config :as config]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.react :refer [copy-to-clipboard]]
            [taoensso.timbre :as log]))

;; Work in progress namespace responsible for push notifications and interacting
;; with Firebase Cloud Messaging.

(handlers/register-handler-db
 :update-fcm-token
 (fn [db [_ fcm-token]]
   (assoc-in db [:notifications :fcm-token] fcm-token)))

;; NOTE: Only need to explicitly request permissions on iOS.
(defn request-permissions []
  (.requestPermissions (.-default rn/react-native-fcm)))

(defn get-fcm-token []
    (-> (.getFCMToken (aget rn/react-native-fcm "default"))
        (.then (fn [x]
                 (when config/notifications-wip-enabled?
                   (log/debug "get-fcm-token: " x)
                   (dispatch [:update-fcm-token x]))))))

(defn on-refresh-fcm-token []
  (.on (.-default rn/react-native-fcm)
       (.-RefreshToken (.-FCMEvent rn/react-native-fcm))
       (fn [x]
         (log/debug "on-refresh-fcm-token: " x)
         (dispatch [:update-fcm-token x]))))

;; TODO(oskarth): Only called in background on iOS right now.
;; NOTE(oskarth): Hardcoded data keys :sum and :msg in status-go right now.
(defn on-notification []
  (.on (.-default rn/react-native-fcm)
       (.-Notification (.-FCMEvent rn/react-native-fcm))
       (fn [event-js]
         (let [event (js->clj event-js :keywordize-keys true)
               data  (select-keys event [:sum :msg])
               aps   (:aps event)]
           (log/debug "on-notification event: " (pr-str event))
           (log/debug "on-notification aps: "   (pr-str aps))
           (log/debug "on-notification data: "  (pr-str data))))))
