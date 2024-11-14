(ns status-im.common.standard-authentication.standard-auth.slide-button.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im.common.biometric.utils :as biometric]
    [status-im.constants :as constants]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [track-text customization-color auth-button-label on-auth-success on-auth-fail
           auth-button-icon-left size blur? container-style disabled? dependencies keycard-supported?]
    :or   {container-style {:flex 1}}}]
  (let [theme           (quo.theme/use-theme)
        auth-method     (rf/sub [:auth-method])
        biometric-auth? (= auth-method constants/auth-method-biometric)
        on-complete     (rn/use-callback
                         (fn [reset]
                           (rf/dispatch [:standard-auth/authorize
                                         {:on-close              (fn [success?]
                                                                   (js/setTimeout #(reset success?) 200))
                                          :auth-button-icon-left auth-button-icon-left
                                          :theme                 theme
                                          :blur?                 blur?
                                          :keycard-supported?    keycard-supported?
                                          :biometric-auth?       biometric-auth?
                                          :on-auth-success       on-auth-success
                                          :on-auth-fail          on-auth-fail
                                          :auth-button-label     auth-button-label}]))
                         (vec (conj dependencies on-auth-success on-auth-fail)))
        biometric-type  (rf/sub [:biometrics/supported-type])]
    [quo/slide-button
     {:container-style     container-style
      :size                size
      :customization-color customization-color
      :on-complete         on-complete
      :track-icon          (if biometric-auth?
                             (biometric/get-icon-by-type biometric-type)
                             :password)
      :track-text          track-text
      :disabled?           disabled?}]))
