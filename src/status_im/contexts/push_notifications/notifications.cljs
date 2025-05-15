(ns status-im.contexts.push-notifications.notifications
  (:require
    [react-native.push-notification-ios :as pn-ios]))

(def clear-received-notifications pn-ios/remove-all-delivered-notifications)
