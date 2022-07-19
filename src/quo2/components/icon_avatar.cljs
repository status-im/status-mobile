(ns quo2.components.icon-avatar
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]))

(def themes {:light {:primary   {:icon-color       colors/primary-50
                                 :background-color colors/primary-20}
                     :purple {:icon-color       colors/purple-opa-50
                              :background-color colors/purple-opa-20}
                     :indigo {:icon-color       colors/indigo-opa-50
                              :background-color colors/indigo-opa-20}
                     :turquoise {:icon-color       colors/turquoise-opa-50
                                 :background-color colors/turquoise-opa-20}
                     :blue {:icon-color       colors/blue-opa-50
                            :background-color colors/blue-opa-20}
                     :green {:icon-color       colors/green-opa-50
                             :background-color colors/green-opa-20}
                     :yellow {:icon-color       colors/yellow-opa-50
                              :background-color colors/yellow-opa-20}
                     :orange {:icon-color       colors/orange-opa-50
                              :background-color colors/orange-opa-20}
                     :red {:icon-color       colors/red-opa-50
                           :background-color colors/red-opa-20}
                     :pink {:icon-color       colors/pink-opa-50
                            :background-color colors/pink-opa-20}
                     :brown {:icon-color       colors/brown-opa-50
                             :background-color colors/brown-opa-20}
                     :beige {:icon-color       colors/beige-opa-50
                             :background-color colors/beige-opa-20}}
             :dark  {:primary   {:icon-color       colors/primary-60
                                 :background-color colors/primary-20}
                     :purple {:icon-color       colors/purple-opa-60
                              :background-color colors/purple-opa-20}
                     :indigo {:icon-color       colors/indigo-opa-60
                              :background-color colors/indigo-opa-20}
                     :turquoise {:icon-color       colors/turquoise-opa-60
                                 :background-color colors/turquoise-opa-20}
                     :blue {:icon-color       colors/blue-opa-60
                            :background-color colors/blue-opa-20}
                     :green {:icon-color       colors/green-opa-60
                             :background-color colors/green-opa-20}
                     :yellow {:icon-color       colors/yellow-opa-60
                              :background-color colors/yellow-opa-20}
                     :orange {:icon-color       colors/orange-opa-60
                              :background-color colors/orange-opa-20}
                     :red {:icon-color       colors/red-opa-60
                           :background-color colors/red-opa-20}
                     :pink {:icon-color       colors/pink-opa-60
                            :background-color colors/pink-opa-20}
                     :brown {:icon-color       colors/brown-opa-60
                             :background-color colors/brown-opa-20}
                     :beige {:icon-color       colors/beige-opa-60
                             :background-color colors/beige-opa-20}}})

(def sizes
  {:big 48
   :medium 32
   :small 20})

(defn icon-avatar
  [{:keys [size color dark?]}]
  (let [component-size (size sizes)
        is-theme-dark? (if dark? :dark :light)
        circle-color (get-in themes [is-theme-dark? color :background-color])
        icon-color (get-in themes [is-theme-dark? color :icon-color])
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
     [icons/icon :main-icons/placeholder20 {:container-style {:width  icon-size
                                                              :height icon-size}
                                            :color icon-color}]]))
