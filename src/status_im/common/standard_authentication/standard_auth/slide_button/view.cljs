(ns status-im.common.standard-authentication.standard-auth.slide-button.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [track-text customization-color auth-button-label on-auth-success on-auth-fail
           auth-button-icon-left size blur? container-style disabled? dependencies
           sign-payload]
    :or   {container-style {:flex 1}}}]
  (let [theme                (quo.theme/use-theme)
        slider-icon          (rf/sub [:standard-auth/slider-icon])
        on-complete-callback (rn/use-callback
                              (fn []
                                (if (seq sign-payload)
                                  (rf/dispatch [:standard-auth/authorize-and-sign
                                                {:sign-payload          sign-payload
                                                 :theme                 theme
                                                 :blur?                 blur?
                                                 :on-sign-success       on-auth-success
                                                 :on-sign-error         on-auth-fail
                                                 :auth-button-label     auth-button-label
                                                 :auth-button-icon-left auth-button-icon-left}])
                                  (rf/dispatch [:standard-auth/authorize
                                                {:theme                 theme
                                                 :blur?                 blur?
                                                 :on-auth-success       on-auth-success
                                                 :on-auth-fail          on-auth-fail
                                                 :auth-button-label     auth-button-label
                                                 :auth-button-icon-left auth-button-icon-left}])))
                              (vec (conj dependencies
                                         on-auth-success
                                         on-auth-fail
                                         sign-payload)))
        on-slider-complete   (rn/use-callback
                              (fn [reset-slider-fn]
                                (js/setTimeout #(reset-slider-fn false) 500)
                                (on-complete-callback))
                              [on-complete-callback])]
    [quo/slide-button
     {:container-style     container-style
      :size                size
      :customization-color customization-color
      :on-complete         on-slider-complete
      :track-icon          slider-icon
      :track-text          track-text
      :disabled?           disabled?}]))
