(ns quo2.components.notifications.notification-dot
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(def ^:const size 8)

(defn notification-dot
  [{:keys [style]}]
  [rn/view
   {:accessibility-label :notification-dot
    :style               (merge
                          {:background-color (colors/theme-colors colors/primary-50 colors/primary-60)
                           :width            size
                           :height           size
                           :border-radius    (/ size 2)
                           :position         :absolute
                           :z-index          1}
                          style)}])
