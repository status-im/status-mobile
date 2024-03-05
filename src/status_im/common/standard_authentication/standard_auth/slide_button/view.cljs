(ns status-im.common.standard-authentication.standard-auth.slide-button.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im.common.standard-authentication.standard-auth.authorize :as authorize]
    [status-im.constants :as constants]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [track-text customization-color auth-button-label on-auth-success on-auth-fail
           auth-button-icon-left size blur? container-style disabled?]
    :or   {container-style {:flex 1}}}]
  (let [theme           (quo.theme/use-theme-value)
        auth-method     (rf/sub [:auth-method])
        biometric-auth? (= auth-method constants/auth-method-biometric)
        on-complete     (rn/use-callback
                         (fn [reset]
                           (authorize/authorize {:on-close              #(js/setTimeout reset 200)
                                                 :auth-button-icon-left auth-button-icon-left
                                                 :theme                 theme
                                                 :blur?                 blur?
                                                 :biometric-auth?       biometric-auth?
                                                 :on-auth-success       on-auth-success
                                                 :on-auth-fail          on-auth-fail
                                                 :auth-button-label     auth-button-label}))
                         [theme])]
    [quo/slide-button
     {:container-style     container-style
      :size                size
      :customization-color customization-color
      :on-complete         on-complete
      :track-icon          (if biometric-auth? :i/face-id :password)
      :track-text          track-text
      :disabled?           disabled?}]))
