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
    :options [{:key   :large
               :value "Large"}
              {:key   :small
               :value "Small"}]}
   {:label "Disabled:"
    :key   :disabled?
    :type  :boolean}
   {:label   "Custom Color"
    :key     :color
    :type    :select
    :options (map (fn [color]
                    (let [key (get color :name)]
                      {:key key :value key}))
                  (quo/picker-colors))}])

(defn cool-preview
  []
  (let [state (reagent/atom {:disabled? false
                             :color :blue
                             :size :large})
        color (reagent/cursor state [:color])
        disabled? (reagent/cursor state [:disabled?])
        size (reagent/cursor state [:size])]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60
          :padding-horizontal 40
          :align-items      :center}
         [quo/slide-button {:track-text "We gotta slide"
                            :track-icon :face-id
                            :customization-color @color
                            :size @size
                            :disabled? @disabled?
                            :on-complete (fn []
                                           (js/alert "I don't wanna slide anymore"))}]]]])))

(defn preview-slide-button
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
