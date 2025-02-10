(ns status-im.contexts.push-notifications.notifications
  (:require
   [react-native.notifee :as notifee]))

(def clear-received-notifications notifee/cancel-displayed-notifications)
