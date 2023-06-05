(ns react-native.permissions
  (:require ["react-native-permissions" :refer (check requestMultiple PERMISSIONS RESULTS)]
            [react-native.platform :as platform]))

(def permissions-map
  {:read-external-storage  (cond
                             platform/android? (.-READ_EXTERNAL_STORAGE (.-ANDROID PERMISSIONS)))
   :write-external-storage (cond
                             platform/low-device? (.-WRITE_EXTERNAL_STORAGE (.-ANDROID PERMISSIONS)))
   :camera                 (cond
                             platform/android? (.-CAMERA (.-ANDROID PERMISSIONS))
                             platform/ios?     (.-CAMERA (.-IOS PERMISSIONS)))
   :motion                 (cond platform/ios? (.-MOTION (.-IOS PERMISSIONS)))
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
