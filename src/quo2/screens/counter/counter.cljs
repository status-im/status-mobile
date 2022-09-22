(ns quo2.screens.counter.counter
  (:require [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [reagent.core :as reagent]
            [quo2.components.counter.counter :as quo2]
            [quo2.foundations.colors :as colors]))

(def descriptor [{:label   "Type:"
                  :key     :type
                  :type    :select
                  :options [{:key   :default
                             :value "Default"}
                            {:key   :secondary
                             :value "Secondary"}
                            {:key   :grey
                             :value "Gray"}]}
                 {:label "Outline?:"
                  :key   :outline
                  :type  :boolean}
                 {:label "Value"
                  :key   :value
                  :type  :text}])

(defn cool-preview []
  (let [state (reagent/atom {:value 5 :type :default})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view {:padding-vertical 60
                  :align-items      :center}
         [quo2/counter @state (:value @state)]]]])))

(defn preview-counter []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
