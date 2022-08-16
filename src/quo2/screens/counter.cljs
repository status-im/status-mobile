(ns quo2.screens.counter
  (:require [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [reagent.core :as reagent]
            [quo2.components.counter :as quo2]
            [quo.design-system.colors :as colors]))

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
      [rn/view {:margin-bottom 50
                :padding       16}
       [preview/customizer state descriptor]
       [rn/view {:padding-vertical 60
                 :align-items      :center}
        [quo2/counter @state (:value @state)]]])))

(defn preview-counter []
  [rn/view {:background-color (:ui-background @colors/theme)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
