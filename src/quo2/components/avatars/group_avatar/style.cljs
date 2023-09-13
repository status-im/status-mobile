(ns quo2.components.avatars.group-avatar.style
  (:require [quo2.foundations.colors :as colors]))

(defn container
  [{:keys [container-size customization-color theme]}]
  {:width            container-size
   :height           container-size
   :align-items      :center
   :justify-content  :center
   :border-radius    (/ container-size 2)
   :overflow         :hidden
   :background-color (colors/theme-colors (colors/custom-color customization-color 50)
                                          (colors/custom-color customization-color 60)
                                          theme)})
