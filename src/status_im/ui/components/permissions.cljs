(ns status-im.ui.components.permissions
  (:require [status-im.utils.platform :as platform]
            [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            ["react-native-permissions" :refer (requestMultiple PERMISSIONS RESULTS)]))

(fx/defn store-permissions
  {:events [::store-permissions]}
  [{:keys [db]} permissions]
  {:db (update db :app/permissions merge permissions)})

(def permissions-map
  {:read-external-storage  (cond
                             platform/android? (.-READ_EXTERNAL_STORAGE (.-ANDROID PERMISSIONS)))
   :write-external-storage (cond
                             platform/android? (.-WRITE_EXTERNAL_STORAGE (.-ANDROID PERMISSIONS)))
   :photo-library          (cond
                             platform/ios? (.-PHOTO_LIBRARY (.-IOS PERMISSIONS)))
   :camera                 (cond
                             platform/android? (.-CAMERA (.-ANDROID PERMISSIONS))
                             platform/ios?     (.-CAMERA (.-IOS PERMISSIONS)))
   :record-audio           (cond
                             platform/android? (.-RECORD_AUDIO (.-ANDROID PERMISSIONS))
                             platform/ios?     (.-MICROPHONE (.-IOS PERMISSIONS)))})

(defn all-granted? [permissions]
  (let [permission-vals (distinct (vals permissions))]
    (and (= (count permission-vals) 1)
         (not (#{(.-BLOCKED RESULTS) (.-DENIED RESULTS) (.-UNAVAILABLE RESULTS)} (first permission-vals))))))

(defn check-granted [all permissions]
  (when (and (map? all) (seq permissions))
    (let [to-check-keys (mapv permissions-map permissions)
          to-check      (select-keys all to-check-keys)]
      (all-granted? to-check))))

(defn request-permissions
  [{:keys [permissions on-allowed on-denied]
    :or   {on-allowed #()
           on-denied  #()}}]
  (let [permissions (remove nil? (mapv #(get permissions-map %) permissions))]
    (if (empty? permissions)
      (on-allowed)
      (-> (requestMultiple (clj->js permissions))
          (.then (fn [^js response]
                   (let [permission-respone (js->clj response)]
                     (re-frame/dispatch [::store-permissions permission-respone])
                     (if (all-granted? (js->clj permission-respone))
                       (on-allowed)
                       (on-denied)))))
          (.catch on-denied)))))
