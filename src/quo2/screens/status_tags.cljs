(ns quo2.screens.status-tags
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [quo2.foundations.colors :as colors]
            [quo2.components.status-tags :as quo2]))

(def descriptor [{:label "Status"
                  :key :status
                  :type :select
                  :options [{:value "Positive"
                             :key :positive}
                            {:value "Negative"
                             :key :negative}
                            {:value "Pending"
                             :key :pending}]}
                 {:label "Size"
                  :key :size
                  :type :select
                  :options [{:value "Small"
                             :key :small}
                            {:value "Large"
                             :key :large}]}])

(defn cool-preview []
  (let [state (reagent/atom {:status :positive
                             :size :small})]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [rn/view {:flex 1}
        [preview/customizer state descriptor]]
       [rn/view {:padding-vertical 60
                 :flex-direction   :row
                 :justify-content  :center}
        [quo2/status-tag @state]]])))

(defn preview-status-tags []
  [rn/view {:background-color (colors/theme-colors colors/white
                                                   colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
