(ns quo2.components.icon-avatar
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]))

(def sizes
{:big 48
 :medium 32
 :small 20})

(defn icon-avatar 
  [{:keys [size]}]
  (let [component-size (size sizes)
        icon-size (case size
                    :big 20
                    :medium 16
                    :small 12)]
    [rn/view {:style {:width component-size
                      :height component-size
                      :border-radius component-size
                      :background-color colors/primary-50-opa-20
                      :justify-content :center
                      :align-items :center}}
     [icons/icon :main-icons/placeholder20 {:container-style {:width  icon-size
                                                              :height icon-size}
                                            :color "nil"}]]))
