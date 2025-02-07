(ns status-im.contexts.push-notifications.remote-token
  (:require
   ["react-native-notifications" :refer (Notifications)]
   [promesa.core :as promesa]))

(defn- create-registration-success-sub
  [on-registered-success]
  (.registerRemoteNotificationsRegistered
   (.events Notifications)
   (fn [^js event]
     (on-registered-success (.-deviceToken event)))))

(defn- create-registration-fail-sub
  [on-registered-fail]
  (.registerRemoteNotificationsRegistrationFailed
   (.events Notifications)
   (fn [^js event]
     (on-registered-fail
      {:error (ex-info "Failed to register for remote token"
                       {:code (.-code event)
                        :description (.-localizedDescription event)
                        :domain (.-domain event)})}))))

(defn- remove-registration-subs
  [subscriptions]
  (doseq [listener-sub subscriptions]
    (.remove listener-sub)))

(defn- start-registration
  [options]
  (.registerRemoteNotifications Notifications (clj->js options)))

(defn- remote-token-registration-process
  [options]
  (let [state (atom {:subs []})]
    (promesa/create
     (fn [resolver rejector]
       (let [on-finish-registration
             (fn [result]
               (remove-registration-subs (:subs @state))
               (if (:error result)
                 (rejector result)
                 (resolver result)))]
         (swap! state assoc
                :subs [(create-registration-success-sub on-finish-registration)
                       (create-registration-fail-sub on-finish-registration)])
         (start-registration options))))))

(defn request-remote-token
  "Requests and registers a device token for remote push-notifications.
   iOS devices will receive an APNS token,
   and Android devices will receive a FCM token."
  [options]
  (-> (remote-token-registration-process options)
      (promesa/then (fn [token] {:ok token}))))

(comment
  (-> (request-remote-token {})
      (promesa/then tap>)
      (promesa/catch tap>)))
