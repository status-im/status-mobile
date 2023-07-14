(ns quo2.components.notifications.info-count
  (:require [quo2.foundations.colors :as colors]
            [quo2.foundations.typography :as typography]
            [react-native.core :as rn]))

(defn- counter-style
  [customization-color overwritten-styles]
  (merge {:width            16
          :height           16
          :position         :absolute
          :right            22
          :border-radius    6
          :justify-content  :center
          :align-items      :center
          :background-color (colors/custom-color-by-theme customization-color 50 60)}
         overwritten-styles))

(defn info-count
  ([amount]
   (info-count {} amount))
  ([{:keys [customization-color style]
     :or   {customization-color :blue}
     :as   props}
    amount]
   (when (> amount 0)
     [rn/view (assoc props :style (counter-style customization-color style))
      [rn/text
       {:style (merge typography/font-medium
                      typography/label
                      {:color colors/white :text-align :center})}
       amount]])))
