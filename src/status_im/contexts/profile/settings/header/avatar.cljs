(ns ^:workletize status-im.contexts.profile.settings.header.avatar
  (:require [quo.core :as quo]
            [quo.theme :as quo.theme]
            [react-native.reanimated :as reanimated]
            [status-im.contexts.profile.settings.header.style :as style]))

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

(defn view
  [props]
  [:f> f-avatar props])
