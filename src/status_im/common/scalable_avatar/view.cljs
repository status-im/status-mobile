(ns status-im.common.scalable-avatar.view
  (:require [quo.core :as quo]
            [react-native.reanimated :as reanimated]
            [status-im.common.scalable-avatar.style :as style]))

(def scroll-range #js [0 48])
(def scale-range #js [1 0.4])

(defn view
  [{:keys [scroll-y full-name online? profile-picture customization-color border-color]}]
  (let [image-scale (reanimated/interpolate scroll-y scroll-range scale-range :clamp)]
    [reanimated/view {:style (style/wrapper border-color image-scale)}
     [quo/user-avatar
      {:full-name           full-name
       :online?             online?
       :profile-picture     profile-picture
       :status-indicator?   true
       :ring?               true
       :customization-color customization-color
       :size                :big}]]))
