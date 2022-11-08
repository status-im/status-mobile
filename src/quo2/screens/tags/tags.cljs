(ns quo2.screens.tags.tags
  (:require [react-native.core :as rn]
            [quo2.screens.preview :as preview]
            [status-im.ui.components.react :as react]
            [quo2.foundations.colors :as colors]
            [quo2.components.tags.tags :as tags]
            [status-im.react-native.resources :as resources]
            [reagent.core :as reagent]))

(def descriptor [{:label   "Size:"
                  :key     :size
                  :type    :select
                  :options [{:key   32
                             :value "32"}
                            {:key   24
                             :value "24"}]}
                 {:label   "Type:"
                  :key     :type
                  :type    :select
                  :options [{:key   :emoji
                             :value "Emoji"}
                            {:key   :icon
                             :value "Icons"}
                            {:key   :label
                             :value "Label"}]}
                 {:label "Labelled:"
                  :key   :labelled
                  :type  :boolean}
                 {:label "Disabled:"
                  :key   :disabled
                  :type  :boolean}
                 {:label "Blurred background:"
                  :key   :blurred
                  :type  :boolean}])

(defn cool-preview []
  (let [state  (reagent/atom {:size       32
                              :labelled   true
                              :type       :emoji})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150
                 :padding-top    60}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
        [rn/view {:flex              1
                  :justify-content   :center
                  :top               60}
         (when (:blurred @state)
           [rn/view {:flex    1}
            [react/view {:flex-direction :row
                         :height         100}
             [react/image {:source (resources/get-image :community-cover)
                           :style  {:flex               1
                                    :height             100
                                    :border-radius      16}}]]
            [react/view {:flex-direction :row
                         :height          100
                         :position        :absolute
                         :left            0
                         :right           0}
             [react/blur-view {:flex               1
                               :style              {:border-radius      16
                                                    :height             100}
                               :blur-amount        40
                               :overlay-color      (colors/theme-colors
                                                    colors/white-opa-70
                                                    colors/neutral-80-opa-80)}]]])
         [rn/scroll-view {:justify-content    :center
                          :align-items        :center
                          :position           :absolute
                          :padding-horizontal 10}
          [tags/tags (merge @state
                            {:default-active 1
                             :data           [{:id 1 :tag-label "Music"     :resource (resources/get-image :music)}
                                              {:id 2 :tag-label "Lifestyle" :resource (resources/get-image :lifestyle)}
                                              {:id 3 :tag-label "Podcasts"  :resource (resources/get-image :podcasts)}]})]]]]])))
(defn preview-tags []
  [rn/view {:flex             1
            :background-color (colors/theme-colors
                               colors/white
                               colors/neutral-90)}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
