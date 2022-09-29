(ns quo2.screens.wallet.lowest-price
  (:require [quo2.foundations.colors :as colors]
            [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [quo2.components.wallet.lowest-price :as quo2]
            [reagent.core :as reagent]))

(def descriptor [{:label "Top value"
                  :key :top-value
                  :type :text}
                 {:label "Top value background color"
                  :key :top-value-bg-color
                  :type :text}
                 {:label "Top value text color"
                  :key :top-value-text-color
                  :type :text}
                 {:label "Bottom value"
                  :key :bottom-value
                  :type :text}
                 {:label "Bottom value background color"
                  :key :bottom-value-bg-color
                  :type :text}
                 {:label "Bottom value text color"
                  :key :bottom-value-text-color
                  :type :text}
                 {:label "Margin top"
                  :key :margin-top
                  :type :text}])

(defn cool-preview []
  (let [state (reagent/atom {:top-value               20
                             :bottom-value            20
                             :top-value-bg-color      colors/neutral-100
                             :top-value-text-color    colors/white
                             :bottom-value-bg-color   colors/neutral-100
                             :bottom-value-text-color colors/white})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
        [rn/view {:padding-vertical 60
                  :flex-direction   :row
                  :justify-content  :center}
         [quo2/lowest-price @state]]]])))

(defn preview-lowest-price []
  [rn/view {:background-color (colors/theme-colors colors/white
                                                   colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
