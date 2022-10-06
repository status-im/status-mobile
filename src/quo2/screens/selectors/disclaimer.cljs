(ns quo2.screens.selectors.disclaimer
  (:require [quo.react-native :as rn]
            [reagent.core :as reagent]
            [quo2.components.buttons.button :as button]
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
         {:container-style {:margin-bottom 40}
          :on-change #(swap! checked? not)}
         "I agree with the community rules"]
        [button/button
         {:disabled (not @checked?)}
         "submit"]]])))

(defn preview-disclaimer []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
