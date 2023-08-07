(ns status-im2.contexts.quo-preview.numbered-keyboard.keyboard-key
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Blur:"
    :key   :blur?
    :type  :boolean}
   {:label "Disable:"
    :key   :disabled?
    :type  :boolean}
   {:label   "Type"
    :type    :select
    :key     :type
    :options [{:key   :digit
               :value "Digit"}
              {:key   :key
               :value "Key"}
              {:key   :derivation-path
               :value "Derivation Path"}]}])

(defn cool-preview
  []
  (let [state (reagent/atom {:disabled? false
                             :on-press  #(js/alert "pressed" %)
                             :blur?     false
                             :type      :digit})]
    (fn []
      (let [value (case (:type @state)
                    :key             :i/delete
                    :derivation-path nil
                    :digit           1
                    nil)]
        [rn/view {:style {:padding-bottom 150}}
         [rn/view {:style {:flex 1}}
          [preview/customizer state descriptor]]
         [preview/blur-view
          {:style                 {:flex              1
                                   :align-self        :center
                                   :justify-self      :center
                                   :margin-horizontal 20}
           :show-blur-background? (:blur? @state)
           :blur-view-props       (when (:blur? @state)
                                    {:overlay-color colors/neutral-80-opa-80})}
          [quo/keyboard-key @state value]]]))))

(defn preview-keyboard-key
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}}
   [rn/flat-list
    {:style                        {:flex 1}
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
