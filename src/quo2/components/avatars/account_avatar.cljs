(ns quo2.components.avatars.account-avatar
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]
            [quo.theme :as theme]))

(def icon-color-value
  {:dark (get-in colors/customization [:dark :purple])
   :light (get-in colors/customization [:light :purple])})

(defn get-border-radius [size]
  (case size
    80 16
    48 12
    32 10
    24 8
    20 6))

(defn get-inner-icon-sizes [size]
  (case size
    80 36
    48 24
    32 15
    24 11
    20 11))

(defn account-avatar
  [{:keys [size icon]
    :or   {size 80
           icon :main-icons/placeholder}}]
  (let [icon-color           (if (theme/dark?)
                               (:dark icon-color-value)
                               (:light icon-color-value))
        avatar-border-radius (get-border-radius size)
        inner-icon-size      (get-inner-icon-sizes size)]
    [rn/view {:style {:width            size
                      :background-color icon-color
                      :height           size
                      :border-radius    avatar-border-radius
                      :justify-content  :center
                      :align-items      :center}}
     [icons/icon icon
      {:no-color        true
       :container-style {:width  inner-icon-size
                         :height inner-icon-size}}]]))
