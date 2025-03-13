(ns status-im.common.standard-authentication.slide-auth
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [track-text customization-color auth-button-label on-success on-fail
           auth-button-icon-left size blur? container-style disabled? dependencies
           sign-payload]
    :or   {container-style {:flex 1}}}]
  (let [theme              (quo.theme/use-theme)
        slider-icon        (rf/sub [:standard-auth/slider-icon])
        on-slider-complete (rn/use-callback
                            (fn []
                              (rf/dispatch [:standard-auth/authorize
                                            {:theme                 theme
                                             :blur?                 blur?
                                             :on-auth-success       on-success
                                             :on-auth-fail          on-fail
                                             :auth-button-label     (or auth-button-label
                                                                        (i18n/label :t/continue))
                                             :auth-button-icon-left auth-button-icon-left}]))
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
