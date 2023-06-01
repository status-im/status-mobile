(ns status-im2.contexts.quo-preview.reactions.react
  (:require [clojure.string :as string]
            [quo2.components.reactions.reaction :as quo2.reaction]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.constants :as constants]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Count"
    :key   :clicks
    :type  :text}
   {:label   "Emoji"
    :key     :emoji
    :type    :select
    :options (for [reaction (vals constants/reactions)]
               {:key   reaction
                :value (string/capitalize (name reaction))})}
   {:label "Neutral"
    :key   :neutral?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:emoji :reaction/love})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60
          :justify-content  :center
          :flex-direction   :row
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
