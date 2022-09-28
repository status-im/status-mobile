(ns quo2.components.selectors.disclaimer
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.markdown.text :as text]
            [quo2.components.selectors.selectors :as selectors]))

(defn disclaimer [{:keys [checked? on-change accessibility-label]} label]
  [rn/view {:style
            {:display :flex
             :flex 2
             :flex-direction :row
             :background-color
             (colors/theme-colors
              colors/neutral-5
              colors/neutral-80-opa-40)
             :padding 11
             :align-self :stretch
             :border-radius 12
             :border-width 1
             :border-color (colors/theme-colors
                            colors/neutral-20
                            colors/neutral-70)}}
   [selectors/checkbox {:checked? checked?
                        :on-change on-change}]
   [text/text {:accessibility-label accessibility-label
               :size                :paragraph-2
               :style {:margin-left 8}}
    label]])

