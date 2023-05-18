(ns status-im2.contexts.quo-preview.buttons.button
  (:require [quo2.components.buttons.button :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Type:"
    :key     :type
    :type    :select
    :options [{:key   :primary
               :value "Primary"}
              {:key   :secondary
               :value "Secondary"}
              {:key   :grey
               :value "Grey"}
              {:key   :dark-grey
               :value "Dark Grey"}
              {:key   :outline
               :value "Outline"}
              {:key   :ghost
               :value "Ghost"}
              {:key   :danger
               :value "Danger"}
              {:key   :positive
               :value "Positive"}]}
   {:label   "Size:"
    :key     :size
    :type    :select
    :options [{:key   56
               :value "56"}
              {:key   40
               :value "40"}
              {:key   32
               :value "32"}
              {:key   24
               :value "24"}]}
   {:label "Icon:"
    :key   :icon
    :type  :boolean}
   {:label "Above icon:"
    :key   :above
    :type  :boolean}
   {:label "After icon:"
    :key   :after
    :type  :boolean}
   {:label "Before icon:"
    :key   :before
    :type  :boolean}
   {:label "Disabled:"
    :key   :disabled
    :type  :boolean}
   {:label "Label"
    :key   :label
    :type  :text}])

(defn cool-preview
  []
  (let [state  (reagent/atom {:label "Press Me"
                              :size  40})
        label  (reagent/cursor state [:label])
        before (reagent/cursor state [:before])
        after  (reagent/cursor state [:after])
        above  (reagent/cursor state [:above])
        icon   (reagent/cursor state [:icon])]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
        [rn/view
         {:padding-vertical 60
          :flex-direction   :row
          :justify-content  :center}
         [quo2/button
          (merge (dissoc @state
                  :theme
                  :before
                  :after)
                 {:on-press #(println "Hello world!")}
                 (when @above
                   {:above :i/placeholder})
                 (when @before
                   {:before :i/placeholder})
                 (when @after
                   {:after :i/placeholder}))
          (if @icon :i/placeholder @label)]]]])))

(defn preview-button
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
