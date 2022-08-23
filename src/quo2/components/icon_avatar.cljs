(ns quo2.components.icon-avatar
  (:require [quo.react-native :as rn]
            [quo.theme :as theme]
            [quo2.foundations.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]))

(def themes {:light {:primary   {:icon-color       colors/primary-50
                                 :background-color colors/primary-20}
                     :purple {:icon-color       colors/purple-50
                              :background-color colors/purple-20}
                     :indigo {:icon-color       colors/indigo-50
                              :background-color colors/indigo-20}
                     :turquoise {:icon-color       colors/turquoise-50
                                 :background-color colors/turquoise-20}
                     :blue {:icon-color       colors/blue-50
                            :background-color colors/blue-20}
                     :green {:icon-color       colors/green-50
                             :background-color colors/green-20}
                     :yellow {:icon-color       colors/yellow-50
                              :background-color colors/yellow-20}
                     :orange {:icon-color       colors/orange-50
                              :background-color colors/orange-20}
                     :red {:icon-color       colors/red-50
                           :background-color colors/red-20}
                     :pink {:icon-color       colors/pink-50
                            :background-color colors/pink-20}
                     :brown {:icon-color       colors/brown-50
                             :background-color colors/brown-20}
                     :beige {:icon-color       colors/beige-50
                             :background-color colors/beige-20}}
             :dark  {:primary   {:icon-color       colors/primary-60
                                 :background-color colors/primary-20}
                     :purple {:icon-color       colors/purple-60
                              :background-color colors/purple-20}
                     :indigo {:icon-color       colors/indigo-60
                              :background-color colors/indigo-20}
                     :turquoise {:icon-color       colors/turquoise-60
                                 :background-color colors/turquoise-20}
                     :blue {:icon-color       colors/blue-60
                            :background-color colors/blue-20}
                     :green {:icon-color       colors/green-60
                             :background-color colors/green-20}
                     :yellow {:icon-color       colors/yellow-60
                              :background-color colors/yellow-20}
                     :orange {:icon-color       colors/orange-60
                              :background-color colors/orange-20}
                     :red {:icon-color       colors/red-60
                           :background-color colors/red-20}
                     :pink {:icon-color       colors/pink-60
                            :background-color colors/pink-20}
                     :brown {:icon-color       colors/brown-60
                             :background-color colors/brown-20}
                     :beige {:icon-color       colors/beige-60
                             :background-color colors/beige-20}}})

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
