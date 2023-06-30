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

(rf/defn biometric-auth-success
  {:events [:biometric/auth-success]}
  [{:keys [db] :as cofx}]
  (let [key-uid (get-in db [:profile/login :key-uid])]
    (keychain/get-user-password cofx key-uid #(rf/dispatch [:profile/get-user-password-success %]))))

(rf/defn show-message
  [_ message code]
  (let [content (if (get #{"NOT_AVAILABLE" "NOT_ENROLLED"} code)
                  (i18n/label :t/grant-face-id-permissions)
                  message)]
    (when content
      {:utils/show-popup
       {:title   (i18n/label :t/biometric-auth-login-error-title)
        :content content}})))

(rf/defn biometric-auth-fail
  {:events [:biometric/auth-fail]}
  [{:keys [db] :as cofx} code]
  (let [auth-method (get db :auth-method)]
    (rf/merge cofx
              (show-message
               (when-not (or (= code "USER_CANCELED") (= code "USER_FALLBACK"))
                 (i18n/label :t/biometric-auth-error {:code code}))
               code))))
              ;(open-login-callback nil)))))

(re-frame/reg-fx
 :biometric/authenticate
 (fn [options]
   (touch-id/authenticate options)))

(rf/defn biometric-auth
  {:events [:biometric/authenticate]}
  [_]
  {:biometric/authenticate
   {:on-success #(rf/dispatch [:biometric/auth-success])
    :on-fail    #(rf/dispatch [:biometric/auth-fail %])
    :reason     (i18n/label :t/biometric-auth-reason-login)
    :options    (merge
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
                    :cancelText             (i18n/label :cancel)}))}})
