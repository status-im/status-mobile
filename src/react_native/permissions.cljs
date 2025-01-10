(ns react-native.permissions
  (:require
    ["react-native-permissions" :refer
     [check PERMISSIONS requestMultiple requestNotifications RESULTS]]
    [react-native.platform :as platform]
    [taoensso.timbre :as log]))

(def permissions-map
  {:read-external-storage  (cond
                             platform/android? (.-READ_EXTERNAL_STORAGE (.-ANDROID PERMISSIONS)))
   :write-external-storage (cond
                             platform/low-device? (.-WRITE_EXTERNAL_STORAGE (.-ANDROID PERMISSIONS)))
   :read-media-images      (cond
                             platform/android? (.-READ_MEDIA_IMAGES (.-ANDROID PERMISSIONS)))
   :post-notifications     (cond
                             platform/android? (.-POST_NOTIFICATIONS (.-ANDROID PERMISSIONS)))
   :camera                 (cond
                             platform/android? (.-CAMERA (.-ANDROID PERMISSIONS))
                             platform/ios?     (.-CAMERA (.-IOS PERMISSIONS)))
   :record-audio           (cond
                             platform/android? (.-RECORD_AUDIO (.-ANDROID PERMISSIONS))
                             platform/ios?     (.-MICROPHONE (.-IOS PERMISSIONS)))})

(defn all-granted?
  [permissions]
  (let [permission-vals (distinct (vals permissions))]
    (and (= (count permission-vals) 1)
         (not (#{(.-BLOCKED RESULTS) (.-DENIED RESULTS)} (first permission-vals))))))

(defn request-permissions
  [{:keys [permissions on-allowed on-denied]
    :or   {on-allowed #()
           on-denied  #()}}]
  (let [permissions (remove nil? (mapv #(get permissions-map %) permissions))]
    (if (empty? permissions)
      (on-allowed)
      (-> (requestMultiple (clj->js permissions))
          (.then #(if (all-granted? (js->clj %))
                    (on-allowed)
                    (on-denied)))
          (.catch on-denied)))))

(defn permission-granted?
  [permission on-result on-error]
  (-> (check (get permissions-map permission))
      (.then #(on-result (not (#{(.-BLOCKED RESULTS) (.-DENIED RESULTS)} %))))
      (.catch #(on-error %))))

(defn request-notifications
  "`notification-options` is only used on iOS.
    A map with `:status` and `settings` (only for iOS) is passed to the callbacks.
    See https://github.com/zoontek/react-native-permissions?tab=readme-ov-file#requestnotifications."
  [{:keys [notification-options on-allowed on-denied]
    :or   {notification-options #js ["alert"]}}]
  (-> (requestNotifications notification-options)
      (.then (fn [js-response]
               (let [response (js->clj js-response :keywordize-keys true)]
                 (if (= (:status response) "granted")
                   (do
                     (on-allowed response)
                     (log/debug "Notification permission were allowed" response))
                   (do
                     (on-denied response)
                     (log/debug "Notification permission were denied" response))))))))
