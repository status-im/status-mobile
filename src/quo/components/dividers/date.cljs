(ns quo.components.dividers.date
  (:require
    [quo.components.common.separator.view :as separator]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]))

(defn date
  [value]
  (let [theme (quo.theme/use-theme)]
    [rn/view
     {:padding-top    8
      :padding-bottom 12
      :padding-right  20
      :padding-left   60}
     [text/text
      {:weight              :medium
       :accessibility-label :divider-date-text
       :size                :label
       :style               {:color          (colors/theme-colors colors/neutral-50
                                                                  colors/neutral-40
                                                                  theme)
                             :text-transform :capitalize
                             :margin-bottom  4}}
      value]
     [separator/separator]]))
