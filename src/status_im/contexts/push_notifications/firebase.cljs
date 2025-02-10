(ns status-im.contexts.push-notifications.firebase
  (:require
   ["@react-native-firebase/messaging" :as firebase.messaging]
   [promesa.core :as promesa]))

(defn create-remote-background-notification-sub
  [on-remote-background-notification]
  (js/console.log "create sub")
  (.setBackgroundMessageHandler
   (firebase.messaging/default)
   (fn [^js message]
     (js/console.log "receive message sean")
     (js/console.log message)
     (promesa/resolved nil))))

(defn request-remote-token
  [options]
  (-> (.registerDeviceForRemoteMessages (firebase.messaging/default))
      (promesa/then #(.getToken (firebase.messaging/default)))
      (promesa/then (fn [token] {:ok token}))
      (promesa/catch (fn [error] {:error error}))))

(comment
  (create-remote-background-notification-sub #())
  (-> (request-remote-token {})
      (promesa/then tap>)
      (promesa/catch tap>))
  (-> (.registerDeviceForRemoteMessages (firebase.messaging/default))
      (promesa/then tap>)
      (promesa/catch tap>))
  (-> (.getToken (firebase.messaging/default))
      (promesa/then tap>)
      (promesa/catch tap>)))
