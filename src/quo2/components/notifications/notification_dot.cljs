(ns quo2.components.notifications.notification-dot
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]))

(defn notification-dot [style]
  [rn/view {:style (merge {:background-color (colors/theme-colors colors/primary-50 colors/primary-60)
                           :width            8
                           :height           8
                           :border-radius    4
                           :position         :absolute
                           :z-index          1}
                          style)}])
