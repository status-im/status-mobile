(ns quo2.screens.selectors.disclaimer
  (:require [quo.react-native :as rn]
            [reagent.core :as reagent]
            [quo2.components.selectors.disclaimer :as quo2]
            [quo2.foundations.colors :as colors]))

(defn cool-preview []
  (let [checked? (reagent/atom false)]
    (fn []
      [rn/view {:margin-bottom      50
                :padding-vertical   16
                :padding-horizontal 20}
       [rn/view {:padding-vertical 60
                 :align-items      :center}
        [quo2/disclaimer
         {:checked?  @checked?
          :on-change #(swap! checked? not)}
         "I agree with the community rules"]]])))

(defn preview-disclaimer []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
