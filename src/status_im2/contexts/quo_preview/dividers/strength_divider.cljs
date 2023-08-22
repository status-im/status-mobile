(ns status-im2.contexts.quo-preview.dividers.strength-divider
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Type"
    :key     :type
    :type    :select
    :options [{:key   :very-weak
               :value "Very weak"}
              {:key   :weak
               :value "Weak"}
              {:key   :okay
               :value "Okay"}
              {:key   :strong
               :value "Strong"}
              {:key   :very-strong
               :value "Very strong"}
              {:key   :alert
               :value "Alert"}
              {:key   :info
               :value "Info"}]}
   {:label "Text(only works for info/alert)"
    :key   :text
    :type  :text}])

(defn cool-preview
  []
  (let [state (reagent/atom {:text "Common password, shouldn’t be used"
                             :type :alert})
        text  (reagent/cursor state [:text])
        type  (reagent/cursor state [:type])]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view {:padding-vertical 60 :background-color colors/neutral-95}
         [quo/strength-divider {:type @type} @text]]]])))

(defn preview-strength-divider
  []
  [rn/view
   {:background-color (colors/theme-colors
                       colors/white
                       colors/neutral-95)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])

