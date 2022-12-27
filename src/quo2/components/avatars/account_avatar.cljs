(ns quo2.components.avatars.account-avatar
  (:require [quo2.components.icon :as icons]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(defn get-border-radius
  [size]
  (case size
    80 16
    48 12
    32 10
    24 8
    20 6))

(defn get-inner-icon-sizes
  [size]
  (case size
    80 36
    48 24
    32 15
    24 11
    20 11))

(defn account-avatar
  [{:keys [size icon color]
    :or   {size  80
           icon  :main-icons/placeholder
           color :purple}}]
  (let [icon-color           (colors/custom-color-by-theme color 50 60)
        avatar-border-radius (get-border-radius size)
        inner-icon-size      (get-inner-icon-sizes size)]
    [rn/view
     {:style {:width            size
              :background-color icon-color
              :height           size
              :border-radius    avatar-border-radius
              :justify-content  :center
              :align-items      :center}}
     [icons/icon icon
      {:no-color true
       :size     inner-icon-size}]]))
