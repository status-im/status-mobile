(ns quo2.components.avatars.group-avatar.style
  (:require [quo2.foundations.colors :as colors]))

(defn container
  [{:keys [container-size customization-color theme]}]
  (let [color (if (keyword? customization-color)
                customization-color
                (get colors/chat-color->customization-color customization-color :blue))]
    {:width            container-size
     :height           container-size
     :align-items      :center
     :justify-content  :center
     :border-radius    (/ container-size 2)
     :overflow         :hidden
     :background-color (colors/theme-colors (colors/custom-color color 50)
                                            (colors/custom-color color 60)
                                            theme)}))
