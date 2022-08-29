(ns quo2.components.icon-avatar
  (:require [quo.react-native :as rn]
            [quo.theme :as theme]
            [quo2.components.avatar.avatar-themes :refer [themes]]
            [status-im.ui.components.icons.icons :as icons]))

(def sizes
  {:big 48
   :medium 32
   :small 20})

(defn icon-avatar
  [{:keys [size icon color]}]
  (let [component-size (size sizes)
        theme (theme/get-theme)
        circle-color (get-in themes [theme color :background-color])
        icon-color (get-in themes [theme color :icon-color])
        icon-size (case size
                    :big 20
                    :medium 16
                    :small 12)]
    [rn/view {:style {:width component-size
                      :height component-size
                      :border-radius component-size
                      :background-color circle-color
                      :justify-content :center
                      :align-items :center}}
     [icons/icon icon {:container-style {:width  icon-size
                                         :height icon-size}
                       :color icon-color}]]))
