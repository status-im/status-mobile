(ns quo2.foundations.customization-colors
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(defn get-overlay-color
  [theme pressed? customization-color]
  (let [community-color (string? customization-color)]
    (if (or community-color (= theme :light))
      (colors/alpha colors/black (if pressed? 0.2 0))
      (colors/alpha colors/black (if pressed? 0 0.2)))))

(defn overlay
  [{:keys [theme pressed? customization-color]}]
  [rn/view
   {:position         :absolute
    :top              0
    :left             0
    :right            0
    :bottom           0
    :background-color (get-overlay-color theme pressed? customization-color)}])
