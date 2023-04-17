(ns status-im2.contexts.quo-preview.reactions.react
  (:require [quo2.components.reactions.reaction :as quo2.reaction]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Count"
    :key   :clicks
    :type  :text}
   {:label   "Emoji"
    :key     :emoji
    :type    :select
    :options [{:key   :main-icons/love16
               :value "Love"}
              {:key   :main-icons/thumbs-up16
               :value "Thumbs Up"}
              {:key   :main-icons/thumbs-down16
               :value "Thumbs Down"}
              {:key   :main-icons/laugh16
               :value "Laugh"}
              {:key   :main-icons/sad16
               :value "Sad"}]}
   {:label "Neutral"
    :key   :neutral?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:emoji :main-icons/love16})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60
          :align-items      :center}
         [quo2.reaction/reaction @state]
         [quo2.reaction/add-reaction @state]]]])))

(defn preview-react
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
