(ns quo2.components.dividers.date
  (:require [quo2.components.markdown.text :as text]
            [quo2.components.separator :as separator]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(defn date
  [value]
  [rn/view
   {:margin-vertical 12
    :padding-right   20
    :padding-left    60}
   [text/text
    {:weight              :medium
     :accessibility-label :divider-date-text
     :size                :label
     :style               {:color          (colors/theme-colors colors/neutral-50 colors/neutral-40)
                           :text-transform :capitalize
                           :margin-bottom  4}}
    value]
   [separator/separator]])
