(ns status-im2.contexts.syncing.standard-authentication.view
  (:require
   [quo2.core :as quo]
   [quo2.theme :as quo.theme]
   [utils.re-frame :as rf]
   [react-native.touch-id :as biometric]
   [utils.i18n :as i18n]
   [status-im2.contexts.syncing.standard-authentication.enter-password :as enter-password]
   [react-native.core :as rn]))

(defn authorize
  [{:keys [set-code use-biometric-auth auth-success auth-fail fallback-button-label theme]}]
  (biometric/get-supported-type
   (fn [biometric-type]
     (if (and use-biometric-auth biometric-type)
       (biometric/authenticate
        {:reason     (i18n/label :t/biometric-auth-confirm-message)
         :on-success (fn [response]
                       (auth-success)
                       (js/console.log "response" response))
         :on-fail    (fn [error]
                       (js/console.log "Authentication Failed. Error:" error)
                       (auth-fail)
                       (rf/dispatch [:show-bottom-sheet
                                     {:theme theme
                                      :content (fn [] [enter-password/view set-code])}]))
         :options    {}})
       (rf/dispatch [:show-bottom-sheet
                     {:content (fn [] [enter-password/view set-code fallback-button-label])}])))))

(defn- view-internal
  [{:keys [theme
           track-text
           customization-color
           fallback-button-label
           enter-pass-callback
           use-biometric-auth
           on-auth-success
           on-auth-fail]}]
  [rn/view {:style {:width "100%"}}
   [quo/slide-button
    {:customization-color customization-color
     :on-complete         #(authorize {:theme                 theme
                                       :set-code              enter-pass-callback
                                       :use-biometric-auth    use-biometric-auth
                                       :auth-success          on-auth-success
                                       :auth-fail             on-auth-fail
                                       :fallback-button-label fallback-button-label})
     :track-icon          :i/face-id
     :track-text          track-text}]])

(def view (quo.theme/with-theme view-internal))
