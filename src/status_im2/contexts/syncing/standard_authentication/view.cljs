(ns status-im2.contexts.syncing.standard-authentication.view
  (:require
   [quo2.core :as quo]
   [utils.re-frame :as rf]
   [react-native.touch-id :as biometric]
   [utils.i18n :as i18n]
   [status-im2.contexts.syncing.standard-authentication.enter-password :as enter-password]
   [status-im2.contexts.syncing.standard-authentication.style :as style]
   [react-native.core :as rn]))

(defn authorize [set-code use-biometric-auth auth-success auth-fail fallback-button-label]
  (biometric/get-supported-type
   (fn [biometric-type]
     (if (and use-biometric-auth biometric-type)
       (biometric/authenticate
        {:reason        (i18n/label :t/biometric-auth-confirm-message)
         :on-success    (fn [response]
                          (auth-success)
                          (js/console.log "response" response))
         :on-fail       (fn [error]
                          (js/console.log "Authentication Failed. Error:" error)
                          (auth-fail)
                          (rf/dispatch [:show-bottom-sheet
                                        {:content (fn [] [enter-password/view set-code])}]))
         :options       {}})
           ;; If biometric authentication is not supported, fallback to password-based authentication
       (rf/dispatch [:show-bottom-sheet
                     {:content (fn [] [enter-password/view set-code fallback-button-label])}])))))

(defn view
  [{:keys [track-text
           thumb-color
           track-text-color
           customization-color
           fallback-button-label
           enter-pass-callback
           use-biometric-auth
           on-auth-success
           on-auth-fail]}]
  [rn/view {:style style/main-container}
   [quo/slide-button
    {:customization-color   customization-color
     :on-complete           #(authorize enter-pass-callback use-biometric-auth on-auth-success on-auth-fail fallback-button-label)
     :thumb-color           thumb-color
     :track-icon            :face-id
     :track-text            track-text
     :track-text-color      track-text-color}]])
