(ns status-im.common.biometric.events
  (:require
    [react-native.biometrics :as biometrics]
    [status-im.common.biometric.utils :as utils]
    [status-im.common.keychain.events :as keychain]
    [status-im.constants :as constants]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(rf/reg-fx
 :biometric/get-supported-type
 (fn []
   ;;NOTE: if we can't save user password, we can't use biometric
   (keychain/can-save-user-password?
    (fn [can-save?]
      (when (and can-save? (not utils/android-device-blacklisted?))
        (-> (biometrics/get-supported-type)
            (.then (fn [type]
                     (rf/dispatch [:biometric/set-supported-type type])))))))))

(rf/reg-event-fx
 :biometric/set-supported-type
 (fn [{:keys [db]} [supported-type]]
   {:db (assoc db :biometric/supported-type supported-type)}))

(rf/reg-event-fx
 :biometric/show-message
 (fn [_ [error]]
   (let [code    (ex-cause error)
         content (if (#{::biometrics/not-enrolled
                        ::biometrics/not-available}
                      code)
                   (i18n/label :t/grant-face-id-permissions)
                   (i18n/label :t/biometric-auth-error {:code code}))]
     {:effects.utils/show-popup
      {:title   (i18n/label :t/biometric-auth-login-error-title)
       :content content}})))


(rf/reg-fx
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

(rf/reg-event-fx
 :biometric/authenticate
 (fn [_ [opts]]
   {:biometric/authenticate opts}))

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
