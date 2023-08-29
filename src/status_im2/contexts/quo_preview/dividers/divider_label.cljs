(ns status-im2.contexts.quo-preview.dividers.divider-label
  (:require [quo2.components.dividers.divider-label :as divider-label]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Label:"
    :key   :label
    :type  :text}
   {:label   "Chevron position:"
    :key     :chevron-position
    :type    :select
    :options [{:key   :left
               :value "Left"}
              {:key   :right
               :value "Right"}]}
   {:label "Counter value:"
    :key   :counter-value
    :type  :text}
   {:label "Increase padding top:"
    :key   :increase-padding-top?
    :type  :boolean}
   {:label "Blur:"
    :key   :blur?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:label                 "Welcome"
                             :chevron-position      :left
                             :counter-value         0
                             :increase-padding-top? true
                             :blur?                 false})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view {:padding-vertical 60}
         [divider-label/divider-label @state]]]])))

(defn preview-divider-label
  []
  [rn/view
   {:background-color (colors/theme-colors
                       colors/white
                       colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
