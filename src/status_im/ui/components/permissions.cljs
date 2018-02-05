(ns status-im.ui.components.permissions
  (:require [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(def permissions-class (.-PermissionsAndroid rn-dependencies/react-native))

(def permissions-map
  {:read-external-storage  "android.permission.READ_EXTERNAL_STORAGE"
   :write-external-storage "android.permission.WRITE_EXTERNAL_STORAGE"
   :camera                 "android.permission.CAMERA"})

(defn all-granted? [permissions]
  (let [permission-vals (distinct (vals permissions))]
    (and (= (count permission-vals) 1)
         (not= (first permission-vals) "denied"))))

(defn request-permissions [permissions then else]
  (if platform/android?
    (letfn [(else-fn [] (when else (else)))]
      (let [permissions (mapv #(get permissions-map %) permissions)]
        (-> (.requestMultiple permissions-class (clj->js permissions))
            (.then #(if (all-granted? (js->clj %))
                      (then)
                      (else-fn)))
            (.catch else-fn))))

    (then)))
