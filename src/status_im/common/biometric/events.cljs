(ns status-im.common.biometric.events
  (:require
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [react-native.async-storage :as async-storage]
    [react-native.platform :as platform]
    [react-native.touch-id :as touch-id]
    [status-im.common.keychain.events :as keychain]
    [status-im.constants :as constants]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def android-device-blacklisted?
  (= (:brand (native-module/get-device-model-info)) "bannedbrand"))

(defn get-supported-type
  [callback]
  (cond platform/ios?     (touch-id/get-supported-type callback)
        platform/android? (if android-device-blacklisted?
                            (callback nil)
                            (touch-id/get-supported-type callback))
        :else             (callback nil)))

(defn get-label-by-type
  [biometric-type]
  (case biometric-type
    :fingerprint (i18n/label :t/biometric-fingerprint)
    :FaceID      (i18n/label :t/biometric-faceid)
    (i18n/label :t/biometric-touchid)))

(re-frame/reg-fx
 :biometric/get-supported-biometric-type
 (fn []
   ;;NOTE: if we can't save user password, we can't use biometric
   (keychain/can-save-user-password?
    (fn [can-save?]
      (when can-save?
        (get-supported-type #(rf/dispatch [:biometric/get-supported-biometric-type-success %])))))))

(rf/defn get-supported-biometric-auth-success
  {:events [:biometric/get-supported-biometric-type-success]}
  [{:keys [db]} supported-type]
  {:db (assoc db :biometric/supported-type supported-type)})

(rf/defn show-message
  {:events [:biometric/show-message]}
  [_ code]
  (let [handle-error? (and code
                           (not (contains? #{constants/biometric-error-user-canceled
                                             constants/biometric-error-user-fallback}
                                           code)))
        content       (if (#{constants/biometric-error-not-available
                             constants/biometric-error-not-enrolled}
                           code)
                        (i18n/label :t/grant-face-id-permissions)
                        (i18n/label :t/biometric-auth-error {:code code}))]
    (when handle-error?
      {:effects.utils/show-popup
       {:title   (i18n/label :t/biometric-auth-login-error-title)
        :content content}})))

(defn- supress-biometry-error-key
  [key-uid]
  (keyword (str "biometric/supress-not-enrolled-error-" key-uid)))

;; NOTE: if the account had biometrics registered, but it's not enrolled at the moment,
;; we should show the error message only once and supress further "NOT_ENROLLED" errors
;; until biometry is enrolled again. Note that we can only know that when :biometric/authenticate
;; is dispatched and fails with "NOT_ENROLLED", since :biometric/get-supported-biometric-type
;; only tells us what kind of biometric is available on the device, but it doesn't know of its
;; enrollment status.
(re-frame/reg-fx
 :biometric/supress-not-enrolled-error
 (fn [[key-uid dispatch-event]]
   (let [storage-key (supress-biometry-error-key key-uid)]
     (-> (async-storage/get-item storage-key identity)
         (.then (fn [item]
                  (when (not item)
                    (rf/dispatch dispatch-event)
                    (async-storage/set-item! storage-key true))))
         (.catch (fn [err]
                   (log/error "Couldn't supress biometry NOT_ENROLLED error"
                              {:key-uid key-uid
                               :event   :biometric/supress-not-enrolled-error
                               :error   err})))))))

;; NOTE: when biometrics is re-enrolled, we erase the flag in async-storage to assure
;; the "NOT_ENROLLED" error message will be shown again if biometrics is un-enrolled
;; in the future.
(re-frame/reg-fx
 :biometric/reset-not-enrolled-error
 (fn [key-uid]
   (let [storage-key (supress-biometry-error-key key-uid)]
     (-> (async-storage/get-item storage-key identity)
         (.then (fn [supress?]
                  (when supress?
                    (async-storage/set-item! storage-key nil))))
         (.catch (fn [err]
                   (log/error "Couldn't reset supressing biometry NOT_ENROLLED error"
                              {:key-uid key-uid
                               :event   :biometric/reset-not-enrolled-error
                               :error   err})))))))

(re-frame/reg-fx
 :biometric/authenticate
 (fn [options]
   (touch-id/authenticate
    (merge
     {:reason  (i18n/label :t/biometric-auth-reason-login)
      :options (merge
                {:unifiedErrors true}
                (when platform/ios?
                  {:passcodeFallback false
                   :fallbackLabel    (i18n/label :t/biometric-auth-login-ios-fallback-label)})
                (when platform/android?
                  {:title                  (i18n/label :t/biometric-auth-android-title)
                   :imageColor             :blue
                   :imageErrorColor        :red
                   :sensorDescription      (i18n/label :t/biometric-auth-android-sensor-desc)
                   :sensorErrorDescription (i18n/label :t/biometric-auth-android-sensor-error-desc)
                   :cancelText             (i18n/label :t/cancel)}))}
     options))))

(rf/defn authenticate
  {:events [:biometric/authenticate]}
  [_ opts]
  {:biometric/authenticate opts})
