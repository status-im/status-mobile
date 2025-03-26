(ns status-im.common.standard-authentication.slide-sign
  (:require
    [quo.context :as quo.context]
    [quo.core :as quo]
    [react-native.core :as rn]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [sign-payload on-success on-fail track-text customization-color auth-button-label
           auth-button-icon-left size blur? container-style disabled? dependencies]
    :or   {container-style {:flex 1}}}]
  (let [theme              (quo.context/use-theme)
        slider-icon        (rf/sub [:standard-auth/slider-icon])
        on-slider-complete (rn/use-callback
                            (fn []
                              (when (seq sign-payload)
                                (rf/dispatch [:standard-auth/authorize-and-sign
                                              {:sign-payload          sign-payload
                                               :theme                 theme
                                               :blur?                 blur?
                                               :on-sign-success       on-success
                                               :on-sign-error         on-fail
                                               :auth-button-label     auth-button-label
                                               :auth-button-icon-left auth-button-icon-left}])))
                            (vec (conj dependencies
                                       on-success
                                       on-fail
                                       sign-payload)))]
    [quo/slide-button
     {:container-style     container-style
      :size                size
      :customization-color customization-color
      :on-complete         on-slider-complete
      :track-icon          slider-icon
      :track-text          track-text
      :disabled?           disabled?}]))
