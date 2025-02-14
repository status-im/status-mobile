(ns status-im.contexts.push-notifications.permissions
  (:require
    [react-native.permissions :as permissions]
    [react-native.rn-notifications :as rn-notifications]))

(def request-notification-permissions permissions/request-notification-permissions)

(def release-notification-permissions rn-notifications/abandon-permissions)
