(ns quo2.screens.dividers.divider-label
  (:require [quo.react-native :as rn]
            [reagent.core :as reagent]
            [quo.previews.preview :as preview]
            [quo2.foundations.colors :as colors]
            [quo2.components.dividers.divider-label :as divider-label]))

(def descriptor [{:label   "Label:"
                  :key     :label
                  :type    :text}
                 {:label   "Chevron position:"
                  :key     :chevron-position
                  :type    :select
                  :options [{:key   :left
                             :value "Left"}
                            {:key   :right
                             :value "Right"}]}
                 {:label   "Counter value:"
                  :key     :counter-value
                  :type    :text}
                 {:label   "Increase padding top:"
                  :key     :increase-padding-top?
                  :type    :boolean}
                 {:label   "Blur:"
                  :key     :blur?
                  :type    :boolean}])

(defn cool-preview []
  (let [state     (reagent/atom {:label "Welcome"
                                 :chevron-position :left
                                 :counter-value 0
                                 :increase-padding-top? true
                                 :blur? false})]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [preview/customizer state descriptor]
       [rn/view {:padding-vertical 60}
        [divider-label/divider-label @state]]])))

(defn preview-divider-label []
  [rn/view  {:background-color (colors/theme-colors
                                colors/white
                                colors/neutral-90)
             :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])