(ns status-im.notifications.android
  (:require ["react-native" :as react-native]
            [quo.platform :as platform]
            [taoensso.timbre :as log]))

(defn pn-android []
  (when platform/android?
    (.-PushNotification ^js (.-NativeModules react-native))))

(defn present-local-notification [opts]
  (.presentLocalNotification ^js (pn-android) (clj->js opts)))

(defn create-channel [{:keys [channel-id channel-name]}]
  (.createChannel ^js (pn-android)
                  #js {:channelId   channel-id
                       :channelName channel-name}
                  #(log/info "Notifications create channel:" %)))

(defn enable-notifications []
  (.enableNotifications ^js (pn-android)))

(defn disable-notifications []
  (.disableNotifications ^js (pn-android)))
