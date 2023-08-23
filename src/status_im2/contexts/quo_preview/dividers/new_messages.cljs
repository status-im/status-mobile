(ns status-im2.contexts.quo-preview.dividers.new-messages
  (:require [quo2.components.dividers.new-messages :as new-messages]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [utils.i18n :as i18n]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Label"
    :key   :label
    :type  :text}
   {:label   "Color"
    :key     :color
    :type    :select
    :options [{:key   :primary
               :value "Primary"}
              {:key   :purple
               :value "Purple"}
              {:key   :indigo
               :value "Indigo"}
              {:key   :turquoise
               :value "Turquoise"}
              {:key   :blue
               :value "Blue"}
              {:key   :green
               :value "Green"}
              {:key   :yellow
               :value "yellow"}
              {:key   :orange
               :value "Orange"}
              {:key   :red
               :value "Red"}
              {:key   :pink
               :value "Pink"}
              {:key   :brown
               :value "Brown"}
              {:key   :beige
               :value "Beige"}]}])
(defn cool-preview
  []
  (let [state (reagent/atom {:label (i18n/label :new-messages-header)
                             :color :primary})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view {:padding-vertical 60}
         [new-messages/new-messages @state]]]])))

(defn preview-new-messages
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
