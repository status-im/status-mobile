(ns status-im2.contexts.quo-preview.counter.step
  (:require [quo2.core :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Type:"
    :key     :type
    :type    :select
    :options [{:key   :neutral
               :value "Neutral (default)"}
              {:key   :active
               :value "Active"}
              {:key   :complete
               :value "Complete"}]}
   {:label "In blur view?"
    :key   :in-blur-view?
    :type  :boolean}
   {:label "Value"
    :key   :value
    :type  :text}])

(defn cool-preview
  []
  (let [state (reagent/atom {:value 5 :type :neutral :in-blur-view? false})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60
          :align-items      :center}
         [quo2/step @state (:value @state)]]]])))

(defn preview-step
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
