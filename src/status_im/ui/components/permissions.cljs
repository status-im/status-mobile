(ns status-im.ui.components.permissions
  (:require [status-im.utils.platform :as platform]
            ["react-native-permissions" :refer (requestMultiple PERMISSIONS RESULTS)]))

(def permissions-map
  {:read-external-storage  (cond
                             platform/android? (.-READ_EXTERNAL_STORAGE (.-ANDROID PERMISSIONS)))
   :write-external-storage (cond
                             platform/android? (.-WRITE_EXTERNAL_STORAGE (.-ANDROID PERMISSIONS)))
   :camera                 (cond
                             platform/android? (.-CAMERA (.-ANDROID PERMISSIONS))
                             platform/ios? (.-CAMERA (.-IOS PERMISSIONS)))
   :record-audio           (cond
                             platform/android? (.-RECORD_AUDIO (.-ANDROID PERMISSIONS))
                             platform/ios? (.-MICROPHONE (.-IOS PERMISSIONS)))})

(defn all-granted? [permissions]
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
