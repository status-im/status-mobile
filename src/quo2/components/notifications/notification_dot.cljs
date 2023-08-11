(ns quo2.components.notifications.notification-dot
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(def ^:const size 8)

(defn dot-background-color
  [customization-color theme]
  (if customization-color
    (colors/theme-colors
     (colors/custom-color customization-color 50)
     (colors/custom-color customization-color 60)
     theme)
    (colors/theme-colors colors/primary-50 colors/primary-60 theme)))

(defn notification-dot
  [{:keys [customization-color style theme]}]
  [rn/view
   {:accessibility-label :notification-dot
    :style               (merge
                          {:background-color (dot-background-color customization-color theme)
                           :width            size
                           :height           size
                           :border-radius    (/ size 2)
                           :position         :absolute
                           :z-index          1}
                          style)}])
