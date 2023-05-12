(ns status-im2.contexts.quo-preview.keycard.keycard
  (:require [status-im2.contexts.quo-preview.preview :as preview]
            [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [quo2.components.keycard.view :as quo2]))

(def descriptor
  [{:label "Holder"
    :key   :holder-name
    :type  :text}
   {:label "Locked?"
    :key   :locked?
    :type  :boolean}])

(defn cool-preview []
  (let [state (reagent/atom {:holder-name nil
                             :locked?     true})]
    (fn
      []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view [preview/customizer state descriptor]
        [quo2/keycard
         {:holder-name? (:holder-name @state)
          :locked?      (:locked? @state)}]]])))

(defn preview-keycard
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white colors/neutral-95)
            :flex             1}}
   [rn/flat-list
    {:style                        {:flex 1}
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
