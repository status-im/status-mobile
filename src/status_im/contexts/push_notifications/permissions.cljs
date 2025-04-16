(ns status-im.contexts.push-notifications.permissions
  (:require
    [react-native.permissions :as permissions]
    [react-native.platform :as platform]
    [react-native.push-notification-ios :as pn-ios]))

(def request-notification-permissions permissions/request-notification-permissions)

(def check-notification-permissions permissions/check-notification-permissions)

(def open-notifications-settings permissions/open-notification-settings)

(defn release-notification-permissions
  []
  (when platform/ios?
    (pn-ios/abandon-permissions)))
