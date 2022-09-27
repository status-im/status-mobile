(ns quo2.components.avatars.icon-avatar
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]))

(def sizes
  {:big 48
   :medium 32
   :small 20})

(defn icon-avatar
  [{:keys [size icon color opacity]
    :or   {opacity 20}}]
  (let [component-size (size sizes)
        circle-color   (colors/custom-color color 50 opacity)
        icon-color     (colors/custom-color-by-theme color 50 60)
        icon-size      (case size
                         :big    20
                         :medium 16
                         :small  12)]
    [rn/view {:style {:width            component-size
                      :height           component-size
                      :border-radius    component-size
                      :background-color circle-color
                      :justify-content  :center
                      :align-items      :center}}
     [icons/icon icon {:container-style {:width  icon-size
                                         :height icon-size}
                       :color           icon-color}]]))
