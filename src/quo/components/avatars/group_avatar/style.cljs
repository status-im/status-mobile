(ns quo.components.avatars.group-avatar.style
  (:require
    [quo.foundations.colors :as colors]))

(defn container
  [{:keys [container-size customization-color theme]}]
  {:width            container-size
   :height           container-size
   :align-items      :center
   :justify-content  :center
   :border-radius    (/ container-size 2)
   :overflow         :hidden
   :background-color (colors/resolve-color customization-color theme)})

(defn avatar-identifier
  [theme]
  {:text-align :center
   :font-size  36
   :color      (colors/theme-colors colors/black
                                    colors/white
                                    theme)})
