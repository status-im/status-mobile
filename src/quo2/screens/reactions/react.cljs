(ns quo2.screens.reactions.react
  (:require [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [reagent.core :as reagent]
            [quo2.components.reactions.react :as quo2]
            [quo2.foundations.colors :as colors]))

(def descriptor [{:label "Count"
                  :key   :clicks
                  :type  :text}
                 {:label "Emoji"
                  :key   :emoji
                  :type  :select
                  :options [{:key :main-icons/love16
                             :value "Love"}
                            {:key :main-icons/thumbs-up16
                             :value "Thumbs Up"}
                            {:key :main-icons/thumbs-down16
                             :value "Thumbs Down"}
                            {:key :main-icons/laugh16
                             :value "Laugh"}
                            {:key :main-icons/sad16
                             :value "Sad"}]}
                 {:label "Neutral"
                  :key   :neutral?
                  :type  :boolean}])

(defn cool-preview []
  (let [state (reagent/atom {:emoji :main-icons/love16})]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [preview/customizer state descriptor]
       [rn/view {:padding-vertical 60
                 :align-items      :center}
        [quo2/render-react @state]
        [quo2/open-reactions-menu @state]]])))

(defn preview-react []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
