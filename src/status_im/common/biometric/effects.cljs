(ns status-im.common.biometric.effects
  (:require
    [react-native.biometrics :as biometrics]
    status-im.common.biometric.effects
    [status-im.common.biometric.utils :as utils]
    [status-im.common.keychain.events :as keychain]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(rf/reg-fx
 :effects.biometric/get-supported-type
 (fn []
   ;;NOTE: if we can't save user password, we can't use biometric
   (keychain/can-save-user-password?
    (fn [can-save?]
      (when (and can-save? (not utils/android-device-blacklisted?))
        (-> (biometrics/get-supported-type)
            (.then (fn [type]
                     (rf/dispatch [:biometric/set-supported-type type])))))))))

(rf/reg-fx
 :effects.biometric/authenticate
 (fn [{:keys [prompt-message on-success on-fail on-cancel on-done]
       :or   {on-done    identity
              on-success identity
              on-cancel  identity
              on-fail    identity}}]
   (-> (biometrics/authenticate
        {:prompt-message          (or prompt-message (i18n/label :t/biometric-auth-reason-login))
         :fallback-prompt-message (i18n/label
                                   :t/biometric-auth-login-ios-fallback-label)
         :cancel-button-text      (i18n/label :t/cancel)})
       (.then (fn [not-canceled?]
                (on-done)
                (if not-canceled?
                  (on-success)
                  (on-cancel))))
       (.catch (fn [err]
                 (on-done)
                 (on-fail err))))))

(rf/reg-fx
 :effects.biometric/check-if-available
 (fn [{:keys [key-uid on-success on-fail]
       :or   {on-success identity
              on-fail    identity}}]
   (keychain/can-save-user-password?
    (fn [can-save?]
      (if-not can-save?
        (on-fail (ex-info "cannot-save-user-password"
                          {:effect :effects.biometric/check-if-available}))
        (-> (biometrics/get-available)
            (.then (fn [available?]
                     (when-not available?
                       (throw (js/Error. "biometric-not-available")))))
            (.then #(keychain/get-auth-method! key-uid))
            (.then (fn [auth-method]
                     (when auth-method
                       (on-success auth-method))))
            (.catch (fn [err]
                      (let [message (.-message err)]
                        (on-fail (ex-info message
                                          {:err    err
                                           :effect :effects.biometric/check-if-available}))
                        (when-not (= message "biometric-not-available")
                          (log/error "Failed to check if biometrics is available"
                                     {:error   err
                                      :key-uid key-uid
                                      :effect  :effects.biometric/check-biometric})))))))))))
