(ns quo2.components.avatars.account-avatar
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [quo2.theme :as theme]))

(defn get-border-radius
  [size]
  (case size
    80 16
    48 12
    32 10
    28 8
    24 8
    20 6
    16 4))

(defn get-emoji-size
  [size]
  (case size
    80 36
    48 24
    32 15
    28 12
    24 12
    20 11
    16 11))

(defn- account-avatar-internal
  [{:keys [size emoji customization-color theme]}]
  (let [icon-color           (colors/theme-colors (colors/custom-color customization-color 50)
                                                  (colors/custom-color customization-color 60)
                                                  theme)
        avatar-border-radius (get-border-radius size)]
    [rn/view
     {:style {:width            size
              :background-color icon-color
              :height           size
              :border-radius    avatar-border-radius
              :justify-content  :center
              :align-items      :center}}
     [rn/text
      {:style {:font-size (get-emoji-size size)}}
      emoji]]))

(def account-avatar (theme/with-theme account-avatar-internal))
