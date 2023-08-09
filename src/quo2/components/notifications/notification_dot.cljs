(ns quo2.components.notifications.notification-dot
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(def ^:const size 8)

(defn dot-background-color
  [customization-color]
  (if customization-color
    (colors/custom-color-by-theme customization-color 50 60)
    (colors/theme-colors colors/primary-50 colors/primary-60)))

(defn notification-dot
  [{:keys [customization-color style]}]
  [rn/view
   {:accessibility-label :notification-dot
    :style               (merge
                          {:background-color (dot-background-color customization-color)
                           :width            size
                           :height           size
                           :border-radius    (/ size 2)
                           :position         :absolute
                           :z-index          1}
                          style)}])
