(ns status-im2.contexts.quo-preview.buttons.slide-button

  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Size:"
    :key     :size
    :type    :select
    :options [{:key   "large"
               :value "large"}
              {:key   "small"
               :value "small"}]}
   {:label "Disabled:"
    :key   :disabled
    :type  :boolean}
   {:label "Label"
    :key   :label
    :type  :text}])

(defn cool-preview
  []
  (let [state (reagent/atom {:size     :large
                             :disabled false
                             :label    "Slide to sign"})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
        [rn/view
         {:padding-vertical   60
          :padding-horizontal 20
          :flex-direction     :row
          :justify-content    :center}
         [quo/slide-button
          (merge @state
                 {:on-end #(js/alert (str "Swiped"))})]]]])))

(defn preview-slide-button
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