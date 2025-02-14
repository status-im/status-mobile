(ns react-native.notifee
  (:require
    ["@notifee/react-native" :as notifee]
    [clojure.string :as string]
    [promesa.core :as promesa]))

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

(defn- notification-permissions->notification-permission-statuses
  [notifee-permissions]
  (let [permission-status (format-auth-status (.-authorizationStatus notifee-permissions))]
    {:authorized?   (= permission-status :authorized)
     :denied?       (= permission-status :denied)
     :undetermined? (= permission-status :not_determined)
     :provisional?  (= permission-status :provisional)}))

(defn check-notification-permissions
  []
  (-> (.getNotificationSettings notifee/default)
      (promesa/then (comp (fn [permissions] {:ok permissions})
                          notification-permissions->notification-permission-statuses))
      (promesa/catch (fn [error]
                       {:error error}))))

(defn request-notification-permissions
  []
  (-> (.requestPermission notifee/default)
      (promesa/then (comp (fn [permissions] {:ok permissions})
                          notification-permissions->notification-permission-statuses))
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

(defn cancel-displayed-notifications
  ([]
   (.cancelDisplayedNotifications notifee/default))
  ([notification-ids]
   (.cancelDisplayedNotifications notifee/default (clj->js notification-ids))))

(comment
  (-> (request-notification-permissions)
      (promesa/then tap>)
      (promesa/catch tap>))

  (-> (request-notification-channel
       {:id   "status-im-notifications-alt"
        :name "Status push notifications Alt"})
      (promesa/then tap>))

  (display-notification
   {:title   "Test title"
    :body    "Test Body"
    :android {:channelId "status-im-notifications-alt"}}))
