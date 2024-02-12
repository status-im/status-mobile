(ns status-im.common.scalable-avatar.view
  (:require [quo.core :as quo]
            [react-native.reanimated :as reanimated]
            [status-im.common.scalable-avatar.style :as style]))
(def scroll-animation-input-range [0 50])
(def header-extrapolation-option
  {:extrapolateLeft  "clamp"
   :extrapolateRight "clamp"})

(defn f-avatar
  [{:keys [scroll-y full-name online? profile-picture customization-color]}]
  (let [avatar-total-size     88
        border-height         4
        translation-to-bottom (fn [scale inverse-scale]
                                (js* "'worklet'")
                                (let [current-avatar-size (* avatar-total-size (- 1 scale))
                                      current-center      (/ current-avatar-size 2)]
                                  (* current-center inverse-scale)))
        transform             (reanimated/use-animated-style
                               (fn []
                                 (js* "'worklet'")
                                 (let [scale         (reanimated/interpolate*
                                                      (.-value scroll-y)
                                                      #js [0 48]
                                                      #js [1 0.4]
                                                      "clamp")
                                       inverse-scale (/ 1 scale)
                                       translation   (translation-to-bottom scale inverse-scale)
                                       bottom        (* border-height inverse-scale (- 1 scale))]
                                   #js
                                           {:transform #js
                                                               [#js {:scale scale}
                                                                #js {:translateX (- translation)}
                                                                #js {:translateY (- translation bottom)}]})))
        theme                 (quo.theme/get-theme)]
    [reanimated/view {:style [transform (style/avatar-container theme)]}
     [quo/user-avatar
      {:full-name           full-name
       :online?             online?
       :profile-picture     profile-picture
       :status-indicator?   true
       :ring?               true
       :customization-color customization-color
       :size                :big}]]))

#_(defn f-avatar
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
                                                            header-extrapolation-option)]
    [reanimated/view
     {:style (style/wrapper {:scale        image-scale-animation
                             :margin-top   image-top-margin-animation
                             :margin       image-side-margin-animation
                             :border-color border-color})}
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
