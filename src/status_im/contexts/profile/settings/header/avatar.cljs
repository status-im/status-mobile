(ns status-im.contexts.profile.settings.header.avatar
  (:require [quo.core :as quo]
            [quo.theme :as quo.theme]
            [react-native.reanimated :as reanimated]
            [status-im.contexts.profile.settings.header.style :as style]))

(def scroll-animation-input-range [0 50])
(def header-extrapolation-option
  {:extrapolateLeft  "clamp"
   :extrapolateRight "clamp"})

(defn f-avatar
  [{:keys [scroll-y full-name online? profile-picture customization-color]}]
  (let [image-scale-animation       (reanimated/interpolate scroll-y
                                                            scroll-animation-input-range
                                                            [1 0.4]
                                                            header-extrapolation-option)
        image-top-margin-animation  (reanimated/interpolate scroll-y
                                                            scroll-animation-input-range
                                                            [0 20]
                                                            header-extrapolation-option)
        image-side-margin-animation (reanimated/interpolate scroll-y
                                                            scroll-animation-input-range
                                                            [0 -20]
                                                            header-extrapolation-option)
        theme                       (quo.theme/get-theme)]
    [reanimated/view
     {:style (style/avatar-container theme
                                     image-scale-animation
                                     image-top-margin-animation
                                     image-side-margin-animation)}
     [quo/user-avatar
      {:full-name           full-name
       :online?             online?
       :profile-picture     profile-picture
       :status-indicator?   true
       :ring?               true
       :customization-color customization-color
       :size                :big}]]))

(defn view
  [props]
  [:f> f-avatar props])
