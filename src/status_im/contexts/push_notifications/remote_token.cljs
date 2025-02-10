(ns status-im.contexts.push-notifications.remote-token
  (:require
    [react-native.rn-notifications :as rn-notifications]))

(def request-remote-token rn-notifications/request-remote-token)
