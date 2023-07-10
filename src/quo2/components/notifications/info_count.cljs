(ns quo2.components.notifications.info-count
  (:require [quo2.foundations.colors :as colors]
            [quo2.foundations.typography :as typography]
            [react-native.core :as rn]))

(defn info-count
  ([amount]
   (info-count {} amount))
  ([props amount]
   (when (> amount 0)
     [rn/view
      (merge props
             {:style (merge {:width            16
                             :height           16
                             :position         :absolute
                             :right            22
                             :border-radius    6
                             :justify-content  :center
                             :align-items      :center
                             :background-color (colors/theme-colors colors/primary-50 colors/primary-60)}
                            (:style props))})
      [rn/text
       {:style (merge typography/font-medium
                      typography/label
                      {:color colors/white :text-align :center})}
       amount]])))
