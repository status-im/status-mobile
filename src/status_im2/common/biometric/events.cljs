(ns status-im2.common.biometric.events
  (:require [react-native.platform :as platform]
            [react-native.touch-id :as touch-id]
            [native-module.core :as native-module]
            [status-im2.common.keychain.events :as keychain]
            [re-frame.core :as re-frame]
            [utils.re-frame :as rf]
            [utils.i18n :as i18n]))

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
  [_ code]
  (let [content (if (#{"NOT_AVAILABLE" "NOT_ENROLLED"} code)
                  (i18n/label :t/grant-face-id-permissions)
                  (when-not (or (= code "USER_CANCELED") (= code "USER_FALLBACK"))
                    (i18n/label :t/biometric-auth-error {:code code})))]
    (when content
      {:utils/show-popup
       {:title   (i18n/label :t/biometric-auth-login-error-title)
        :content content}})))

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
                   :cancelText             (i18n/label :cancel)}))}
     options))))

(rf/defn authenticate
  {:events [:biometric/authenticate]}
  [_ opts]
  {:biometric/authenticate opts})
