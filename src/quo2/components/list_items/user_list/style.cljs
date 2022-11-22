(ns quo2.components.list-items.user-list.style
  (:require [quo2.foundations.colors :as colors]))

(defn user-list-wrapper [can-be-invited? background-type]
  (let [height (if can-be-invited? 56 80)]
    {:width              359
     :border-radius      12
     :padding-vertical   8
     :padding-horizontal 12
     :height             height
     :background-color   (case background-type
                           :5  colors/primary-50-opa-5
                           :10 colors/primary-50-opa-10
                           :transparent :transparent)
     :flex-direction     :column
     :justify-content    :center}))

(def ml-8 {:margin-left 8})

(def row-centered {:flex-direction :row
                   :align-items    :center})

(def icon-container-styles {:width           32
                            :height          32
                            :border-radius   10
                            :border-width    1
                            :border-color    (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10)
                            :justify-content :center
                            :align-items     :center})

(def container (assoc row-centered :justify-content :space-between))

(def cant-be-invited {:flex-direction :row
                      :margin-top     8
                      :margin-left    40})

(def icon-container {:margin-right 2
                     :margin-top   2})

(def cant-be-invited-text-style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)})
