(ns status-im.contexts.push-notifications.permissions
  (:require
   [react-native.notifee :as notifee]
   [react-native.rn-notifications :as rn-notifications]))

(def request-notification-permissions notifee/request-notification-permissions)

(def release-notification-permissions rn-notifications/abandon-permissions)
