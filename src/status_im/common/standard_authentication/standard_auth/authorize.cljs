(ns status-im.common.standard-authentication.standard-auth.authorize
  (:require
    [native-module.core :as native-module]
    [react-native.touch-id :as biometric]
    [status-im.common.standard-authentication.enter-password.view :as enter-password]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn reset-password
  []
  (rf/dispatch [:set-in [:profile/login :password] nil])
  (rf/dispatch [:set-in [:profile/login :error] ""]))

(defn authorize
  [{:keys [biometric-auth? on-auth-success on-auth-fail on-close
           auth-button-label theme blur? auth-button-icon-left]}]
  (let [handle-auth-success (fn [biometric?]
                              (fn [entered-password]
                                (let [sha3-pwd (if biometric?
                                                 (str (security/safe-unmask-data entered-password))
                                                 (native-module/sha3 (str (security/safe-unmask-data
                                                                           entered-password))))]
                                  (on-auth-success sha3-pwd))))
        password-login      (fn [{:keys [on-press-biometrics]}]
                              (rf/dispatch [:show-bottom-sheet
                                            {:on-close on-close
                                             :theme    theme
                                             :shell?   blur?
                                             :content  (fn []
                                                         [enter-password/view
                                                          {:on-enter-password   (handle-auth-success
                                                                                 false)
                                                           :on-press-biometrics on-press-biometrics
                                                           :button-icon-left    auth-button-icon-left
                                                           :button-label        auth-button-label}])}]))
        ; biometrics-login recursively passes itself as a parameter because if the user
        ; fails biometric auth they will be shown the password bottom sheet with an option
        ; to retrigger biometric auth, so they can endlessly repeat this cycle.
        biometrics-login    (fn [on-press-biometrics]
                              (rf/dispatch [:dismiss-keyboard])
                              (biometric/authenticate
                               {:reason     (i18n/label :t/biometric-auth-confirm-message)
                                :on-success (fn [_response]
                                              (on-close)
                                              (rf/dispatch [:standard-auth/on-biometric-success
                                                            (handle-auth-success true)]))
                                :on-fail    (fn [error]
                                              (on-close)
                                              (log/error "Authentication Failed. Error:" error)
                                              (when on-auth-fail (on-auth-fail error))
                                              (password-login {:on-press-biometrics
                                                               #(on-press-biometrics
                                                                 on-press-biometrics)}))}))]
    (biometric/get-supported-type
     (fn [biometric-type]
       (if (and biometric-auth? biometric-type)
         (biometrics-login biometrics-login)
         (do
           (reset-password)
           (password-login {})))))))
