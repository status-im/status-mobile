(ns status-im2.contexts.quo-preview.buttons.button
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.common.resources :as resources]))

(def descriptor
  [{:label   "Type:"
    :key     :type
    :type    :select
    :options [{:key   :primary
               :value "Primary"}
              {:key   :positive
               :value "Positive"}
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
              {:key   :black
               :value "Black"}]}
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
   {:label   "Background:"
    :key     :background
    :type    :select
    :options [{:key   :blur
               :value "Blur"}
              {:key   :photo
               :value "Photo"}]}
   {:label "Icon Only?:"
    :key   :icon-only?
    :type  :boolean}
   {:label "show icon-top "
    :key   :icon-top
    :type  :boolean}
   {:label "show icon-right"
    :key   :icon-right
    :type  :boolean}
   {:label "show icon-left"
    :key   :icon-left
    :type  :boolean}
   {:label "Disabled?:"
    :key   :disabled?
    :type  :boolean}
   {:label "Label"
    :key   :label
    :type  :text}
   {:label   "Customization color:"
    :key     :customization-color
    :type    :select
    :options (map (fn [color]
                    (let [k (get color :name)]
                      {:key k :value k}))
                  (quo/picker-colors))}])

(defn cool-preview
  []
  (let [state               (reagent/atom {:label "Press Me"
                                           :size  40})
        label               (reagent/cursor state [:label])
        icon-left           (reagent/cursor state [:icon-left])
        icon-right          (reagent/cursor state [:icon-right])
        icon-top            (reagent/cursor state [:icon-top])
        icon-only?          (reagent/cursor state [:icon-only?])
        type                (reagent/cursor state [:type])
        customization-color (reagent/cursor state [:customization-color])]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
        [rn/view
         {:padding-vertical 60
          :flex-direction   :row
          :justify-content  :center}
         (when (= :photo (:background @state))
           [rn/image
            {:source (resources/get-mock-image :community-cover)
             :style  {:position :absolute
                      :top      0
                      :left     0
                      :right    0
                      :bottom   0}}])
         [quo/button
          (merge (dissoc @state
                  :customization-color
                  :theme
                  :icon-left
                  :icon-right)
                 {:background (:background @state)
                  :on-press   #(println "Hello world!")}
                 (when (and (= type :primary) customization-color)
                   (:customization-color customization-color))
                 (when @icon-top
                   {:icon-top :i/placeholder})
                 (when @icon-left
                   {:icon-left :i/placeholder})
                 (when @icon-right
                   {:icon-right :i/placeholder}))
          (if @icon-only? :i/placeholder @label)]]]])))

(defn preview-button
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-95)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
