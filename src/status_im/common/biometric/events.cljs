(ns status-im.common.biometric.events
  (:require
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [react-native.biometrics :as biometrics]
    [react-native.platform :as platform]
    [status-im.common.keychain.events :as keychain]
    [status-im.constants :as constants]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def android-device-blacklisted?
  (and platform/android? (= (:brand (native-module/get-device-model-info)) "bannedbrand")))

(defn get-label-by-type
  [biometric-type]
  (condp = biometric-type
    constants/biometrics-type-android (i18n/label :t/biometric-fingerprint)
    constants/biometrics-type-face-id (i18n/label :t/biometric-faceid)
    (i18n/label :t/biometric-touchid)))

(defn get-icon-by-type
  [biometric-type]
  (condp = biometric-type
    constants/biometrics-type-face-id :i/face-id
    :i/touch-id))

(re-frame/reg-fx
 :biometric/get-supported-biometric-type
 (fn []
   ;;NOTE: if we can't save user password, we can't use biometric
   (keychain/can-save-user-password?
    (fn [can-save?]
      (when (and can-save? (not android-device-blacklisted?))
        (-> (biometrics/get-supported-type)
            (.then (fn [type]
                     (rf/dispatch [:biometric/get-supported-biometric-type-success type])))))))))

(rf/defn get-supported-biometric-auth-success
  {:events [:biometric/get-supported-biometric-type-success]}
  [{:keys [db]} supported-type]
  {:db (assoc db :biometric/supported-type supported-type)})

(rf/defn show-message
  {:events [:biometric/show-message]}
  [_ error]
  (let [code    (ex-cause error)
        content (if (#{::biometrics/not-enrolled
                       ::biometrics/not-available}
                     code)
                  (i18n/label :t/grant-face-id-permissions)
                  (i18n/label :t/biometric-auth-error {:code code}))]
    {:effects.utils/show-popup
     {:title   (i18n/label :t/biometric-auth-login-error-title)
      :content content}}))


(re-frame/reg-fx
 :biometric/authenticate
 (fn [{:keys [on-success on-fail prompt-message]}]
   (-> (biometrics/authenticate
        {:prompt-message          (or prompt-message (i18n/label :t/biometric-auth-reason-login))
         :fallback-prompt-message (i18n/label
                                   :t/biometric-auth-login-ios-fallback-label)
         :cancel-button-text      (i18n/label :t/cancel)})
       (.then (fn [not-canceled?]
                (when (and on-success not-canceled?)
                  (on-success))))
       (.catch (fn [err]
                 (when on-fail
                   (on-fail err)))))))

(rf/defn authenticate
  {:events [:biometric/authenticate]}
  [_ opts]
  {:biometric/authenticate opts})

(rf/reg-event-fx
 :biometric/on-enable-success
 (fn [{:keys [db]} [password]]
   (let [key-uid (get-in db [:profile/profile :key-uid])]
     {:db       (assoc db :auth-method constants/auth-method-biometric)
      :dispatch [:keychain/save-password-and-auth-method
                 {:key-uid         key-uid
                  :masked-password password}]})))

(rf/reg-event-fx
 :biometric/enable
 (fn [_ [password]]
   {:dispatch [:biometric/authenticate
               {:on-success #(rf/dispatch [:biometric/on-enable-success password])
                :on-fail    #(rf/dispatch [:biometric/show-message %])}]}))

(rf/reg-event-fx
 :biometric/disable
 (fn [{:keys [db]}]
   (let [key-uid (get-in db [:profile/profile :key-uid])]
     {:db                           (assoc db :auth-method constants/auth-method-none)
      :keychain/clear-user-password key-uid})))

(rf/reg-fx
 :biometric/check-if-available
 (fn [[key-uid callback]]
   (keychain/can-save-user-password?
    (fn [can-save?]
      (when can-save?
        (-> (biometrics/get-available)
            (.then (fn [available?]
                     (when-not available?
                       (throw (js/Error. "biometric-not-available")))))
            (.then #(keychain/get-auth-method! key-uid))
            (.then (fn [auth-method]
                     (when auth-method (callback auth-method))))
            (.catch (fn [err]
                      (when-not (= (.-message err) "biometric-not-available")
                        (log/error "Failed to check if biometrics is available"
                                   {:error   err
                                    :key-uid key-uid
                                    :event   :profile.login/check-biometric}))))))))))
