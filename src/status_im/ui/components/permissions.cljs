(ns status-im.ui.components.permissions
  (:require [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]
            [status-im.ui.components.camera :as camera]
            [status-im.react-native.js-dependencies :as js-dependencies]))

(def permissions-class (.-PermissionsAndroid js-dependencies/react-native))

(def permissions-map
  {:read-external-storage  "android.permission.READ_EXTERNAL_STORAGE"
   :write-external-storage "android.permission.WRITE_EXTERNAL_STORAGE"
   :camera                 "android.permission.CAMERA"})

(defn all-granted? [permissions]
  (let [permission-vals (distinct (vals permissions))]
    (and (= (count permission-vals) 1)
         (not= (first permission-vals) "denied"))))

(defn request-permissions [{:keys [permissions on-allowed on-denied]
                            :or   {on-allowed #()
                                   on-denied  #()}
                            :as   options}]
  (if platform/android?
    (let [permissions (mapv #(get permissions-map %) permissions)]
      (-> (.requestMultiple permissions-class (clj->js permissions))
          (.then #(if (all-granted? (js->clj %))
                    (on-allowed)
                    (on-denied)))
          (.catch on-denied)))

    (if ((set permissions) :camera)
      (camera/request-access-ios on-allowed on-denied)
      (on-allowed))))
