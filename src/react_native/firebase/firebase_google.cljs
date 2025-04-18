(ns react-native.firebase.firebase-google
  (:require
    ["@react-native-firebase/messaging" :as firebase.messaging]
    [promesa.core :as promesa]))

(defn request-remote-token
  [_options]
  (-> (.registerDeviceForRemoteMessages (firebase.messaging/default))
      (promesa/then #(.getToken (firebase.messaging/default)))))

(defn revoke-remote-token
  [_options]
  (.deleteToken (firebase.messaging/default)))
