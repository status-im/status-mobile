(ns status-im2.common.standard-authentication.standard-auth.authorize
  (:require
    [react-native.touch-id :as biometric]
    [status-im2.common.standard-authentication.enter-password.view :as enter-password]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn reset-password
  []
  (rf/dispatch [:set-in [:profile/login :password] nil])
  (rf/dispatch [:set-in [:profile/login :error] ""]))

(defn authorize
  [{:keys [on-enter-password biometric-auth? on-auth-success on-auth-fail on-close
           auth-button-label theme blur? auth-button-icon-left]}]
  (biometric/get-supported-type
   (fn [biometric-type]
     (if (and biometric-auth? biometric-type)
       (biometric/authenticate
        {:reason     (i18n/label :t/biometric-auth-confirm-message)
         :on-success (fn [response]
                       (when on-auth-success (on-auth-success response))
                       (log/info "response" response))
         :on-fail    (fn [error]
                       (log/error "Authentication Failed. Error:" error)
                       (when on-auth-fail (on-auth-fail error))
                       (rf/dispatch [:show-bottom-sheet
                                     {:theme   theme
                                      :shell?  blur?
                                      :content (fn []
                                                 [enter-password/view
                                                  {:on-enter-password on-enter-password}])}]))})
       (do
         (reset-password)
         (rf/dispatch [:show-bottom-sheet
                       {:on-close on-close
                        :theme    theme
                        :shell?   blur?
                        :content  (fn []
                                    [enter-password/view
                                     {:on-enter-password on-enter-password
                                      :button-icon-left  auth-button-icon-left
                                      :button-label      auth-button-label}])}]))))))
