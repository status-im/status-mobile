(ns quo2.components.list-items.menu-item
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors :refer [theme-colors]]
            [quo2.components.markdown.text :as text]
            [quo2.components.icon :as icons]))

(defn themes [type]
  (case type
    :main     {:icon-color (theme-colors colors/neutral-50 colors/neutral-10)
               :background (theme-colors colors/white colors/neutral-90)
               :text-color (theme-colors colors/neutral-100 colors/white)}
    :danger   {:icon-color (theme-colors colors/danger-50 colors/danger-60)
               :background (theme-colors colors/white colors/neutral-90)
               :text-color (theme-colors colors/danger-50 colors/danger-60)}))

(defn menu-item
  [{:keys [type title accessibility-label icon on-press]
    :or   {type :main}}]
  (let [{:keys [icon-color text-color background]} (themes type)]
    [rn/touchable-opacity
     (merge {:accessibility-label accessibility-label
             :style {:background-color background
                     :height 48
                     :flex-direction :row
                     :align-items :center}}
            (when on-press
              {:on-press on-press}))
     [rn/view {:style {:flex-direction :row
                       :flex-grow      0
                       :flex-shrink    1
                       :padding-horizontal  20}}
      [rn/view {:style {:width            20
                        :height           20
                        :align-items      :center
                        :justify-content  :center
                        :margin-right     12}}
       [icons/icon icon {:color icon-color}]]
      [text/text {:weight              :medium
                  :style               {:color text-color}
                  :ellipsize-mode      :tail
                  :number-of-lines     1
                  :size                :paragraph-1}
       title]]]))
