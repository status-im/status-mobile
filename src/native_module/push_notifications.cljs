(ns native-module.push-notifications
  (:require
    ["react-native" :as react-native]
    [taoensso.timbre :as log]))

(defn push-notification
  []
  (when (exists? (.-NativeModules react-native))
    (.-PushNotification ^js (.-NativeModules react-native))))

(defn present-local-notification
  [opts]
  (.presentLocalNotification ^js (push-notification) (clj->js opts)))

(defn clear-message-notifications
  [chat-id]
  (.clearMessageNotifications ^js (push-notification) chat-id))

(defn create-channel
  [{:keys [channel-id channel-name]}]
  (.createChannel ^js (push-notification)
                  #js {:channelId channel-id :channelName channel-name}
                  #(log/info "Notifications create channel:" %)))

(defn enable-notifications
  []
  (.enableNotifications ^js (push-notification)))

(defn disable-notifications
  []
  (.disableNotifications ^js (push-notification)))

(defn add-listener
  [event callback]
  (.addListener ^js (.-DeviceEventEmitter ^js react-native)
                event
                (fn [^js data]
                  (when (and data (.-dataJSON data) callback)
                    (callback (.-dataJSON data))))))
