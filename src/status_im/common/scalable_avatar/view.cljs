(ns status-im.common.scalable-avatar.view
  (:require [quo.core :as quo]
            [quo.theme :as quo.theme]
            [react-native.reanimated :as reanimated]
            [status-im.common.scalable-avatar.style :as style]))
(def scroll-animation-input-range [0 50])
(def header-extrapolation-option
  {:extrapolateLeft  "clamp"
   :extrapolateRight "clamp"})

(defn f-avatar
  [{:keys [scroll-y full-name online? profile-picture customization-color border-color]}]
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
                                                            [-4 -20]
                                                            header-extrapolation-option)
        theme                       (quo.theme/get-theme)]
    [reanimated/view
     {:style (style/wrapper theme
                            image-scale-animation
                            image-top-margin-animation
                            image-side-margin-animation
                            border-color)}
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
