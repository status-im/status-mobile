(ns react-native.firebase.firebase-fdroid
  (:require
    [promesa.core :as promesa]))

(defn request-remote-token
  [_options]
  (promesa/rejected
   (ex-info "Failed to register for FCM token"
            {:description "Cannot register FCM token on FDroid"})))

(defn revoke-remote-token
  [_options]
  (promesa/rejected
   (ex-info "Failed to revoke a FCM token"
            {:description "Cannot revoke a FCM token on FDroid"})))
