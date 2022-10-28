(ns quo2.components.notifications.info-count
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.foundations.typography :as typography]))

<<<<<<< HEAD
<<<<<<< HEAD
=======

>>>>>>> fe838fd84... feat: messages home items
=======
>>>>>>> e1dc2c907... feat: messages home items
(defn info-count [count style]
  (when (> count 0)
    [rn/view {:style (merge {:width            16
                             :height           16
                             :position         :absolute
                             :right            22
                             :border-radius    6
                             :background-color (colors/theme-colors colors/primary-50 colors/primary-60)}
                            style)}
     [rn/text {:style (merge typography/font-medium typography/label {:color colors/white :text-align :center})} count]]))
