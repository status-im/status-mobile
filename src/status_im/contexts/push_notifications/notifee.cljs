(ns status-im.contexts.push-notifications.notifee
  (:require
   ["@notifee/react-native" :as notifee]
   [clojure.string :as string]
   [promesa.core :as promesa]))

;; notification permission settings

(def authorization-statuses (js->clj notifee/AuthorizationStatus))

(defn- format-auth-status-helper
  [auth-status]
  (if (number? auth-status)
    auth-status
    (keyword (string/lower-case auth-status))))

(defn- format-auth-status
  [auth-status-key]
  (->> (cond
         (string? auth-status-key)  auth-status-key
         (keyword? auth-status-key) (str (symbol auth-status-key))
         :else                      (str auth-status-key))
       (string/upper-case)
       (get authorization-statuses)
       (format-auth-status-helper)))

(defn- notifee-settings->notification-auth-statuses
  [notifee-settings]
  (let [auth-status (format-auth-status (.-authorizationStatus notifee-settings))]
    {:authorized? (= auth-status :authorized)
     :denied?     (= auth-status :denied)
     :auth-status auth-status}))

(defn request-notification-settings
  []
  (-> (.getNotificationSettings notifee/default)
      (promesa/then (comp (fn [settings] {:ok settings})
                          notifee-settings->notification-auth-statuses))
      (promesa/catch (fn [error]
                       {:error error}))))

(defn request-notification-permissions
  []
  (-> (.requestPermission notifee/default)
      (promesa/then (comp (fn [settings] {:ok settings})
                          notifee-settings->notification-auth-statuses))
      (promesa/catch (fn [error]
                       {:error error}))))

(defn request-notification-channel
  [options]
  (-> (.createChannel notifee/default (clj->js options))
      (promesa/then (fn [channel-id]
                      {:ok {:channel-id channel-id}}))
      (promesa/catch (fn [error]
                       {:error error}))))

(defn display-notification
  [options]
  (.displayNotification notifee/default (clj->js options)))

(comment
  (-> (request-notification-permissions)
      (promesa/then tap>)
      (promesa/catch tap>))

  (-> (request-notification-channel
       {:id "status-im-notifications-alt"
        :name "Status push notifications Alt"})
      (promesa/then tap>))

  (display-notification
   {:title "Test title"
    :body "Test Body"
    :android {:channelId "status-im-notifications-alt"}}))
