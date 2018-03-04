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

;; TODO (yenda) this is a workaround to get a callback for the
;; permission request on android. When prompting the user for a
;; permission request the promise never executes the  .then
;; however there is a status-module-initialized event that is triggered
;; when the user grants or denies the permissions.
;; So we reset this atom in request-permissions for android
;; and if it is not nil we dispatch it in the ::status-module-initialized event
;; this time .then will be called
;; if the user denies he will be asked a second time but it is better than
;; nothing happening when the user accepts.
;; FIX: (yenda) find out why the promise doesn't executes it's then the first time,
;; might have something to do with the fact that the app is apparently resumed once
;; the user responds to the pop-up (that's what triggers status-module-initialized event)
(def pending-permissions-event (atom nil))

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
          (.catch on-denied))
      (if @pending-permissions-event
        (reset! pending-permissions-event nil)
        (reset! pending-permissions-event [:request-permissions options])))

    (if ((set permissions) :camera)
      (camera/request-access-ios on-allowed on-denied)
      (on-allowed))))
