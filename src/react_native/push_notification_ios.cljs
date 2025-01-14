(ns react-native.push-notification-ios
  (:require
    ["@react-native-community/push-notification-ios" :default pn-ios]
    [promesa.core :as promesa]
    [react-native.platform :as platform]))

(defn present-local-notification
  [title message user-info]
  (.presentLocalNotification ^js pn-ios #js {:alertBody message :alertTitle title :userInfo user-info}))

(defn add-listener
  [event callback]
  (.addEventListener ^js pn-ios event callback)
  #js {:remove (fn []
                 (.removeEventListener ^js pn-ios event))})

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

(defn- create-registration-success-listener
  [on-registered-success]
  (add-listener "register"
                (fn [token]
                  (on-registered-success token))))

(defn- create-registration-fail-listener
  [on-registered-fail]
  (add-listener "registrationError"
                (fn [event]
                  (on-registered-fail
                   (ex-info "Failed to register for remote token"
                            {:code        (.-code event)
                             :description (.-message event)
                             :details     (.-details event)})))))

(defn- remove-registration-listeners
  [listeners]
  (doseq [listener listeners]
    (.remove listener)))

(defn- start-registration
  [_options]
  (request-permissions))

(defn- remote-token-registration-process
  [options]
  (let [state (atom {:listeners []})]
    (promesa/create
     (fn [resolver rejector]
       (let [on-finish-registration
             (fn [result]
               (remove-registration-listeners (:listeners @state))
               (if (:error result)
                 (rejector result)
                 (resolver result)))]
         (swap! state assoc
           :listeners
           [(create-registration-success-listener on-finish-registration)
            (create-registration-fail-listener on-finish-registration)])
         (start-registration options))))))

(defn- fail-request-for-android
  [_options]
  (promesa/rejected
   (ex-info "Failed to register for APNS token"
            {:description "Cannot register APNS token on Android"})))

(defn request-remote-token
  "Requests and registers a device token for remote push-notifications for iOS."
  [options]
  (if platform/ios?
    (remote-token-registration-process options)
    (fail-request-for-android options)))
