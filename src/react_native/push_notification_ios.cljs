(ns react-native.push-notification-ios
  (:require ["@react-native-community/push-notification-ios" :default pn-ios]))

(defn present-local-notification
  [title message user-info]
  (.presentLocalNotification ^js pn-ios #js {:alertBody message :alertTitle title :userInfo user-info}))

(defn add-listener
  [event callback]
  (.addEventListener ^js pn-ios event callback))

(defn request-permissions
  []
  (-> (.requestPermissions ^js pn-ios)
      (.then #())
      (.catch #())))

(defn abandon-permissions
  []
  (.abandonPermissions ^js pn-ios))

(defn remove-all-delivered-notifications
  []
  (.removeAllDeliveredNotifications ^js pn-ios))
