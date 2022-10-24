(ns status-im2.contexts.quo-preview.tags.tag
  (:require [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [quo2.foundations.colors :as colors]
            [quo2.components.tags.tag :as tag]
            [status-im.ui.components.react :as react]
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
                  :key   :labelled?
                  :type  :boolean}
                 {:label "Disabled:"
                  :key   :disabled?
                  :type  :boolean}
                 {:label "Blurred background:"
                  :key   :blurred?
                  :type  :boolean}])

(defn cool-preview []
  (let [state  (reagent/atom {:size       32
                              :labelled?   true
                              :type       :emoji})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:style {:padding-bottom 150
                         :padding-top    60}}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
        [rn/view {:style {:flex               1
                          :justify-content    :center
                          :top                60
                          :padding-horizontal 16}}
         (when (:blurred? @state)
           [rn/view {:style {:flex    1
                             :height             100}}
            [react/image {:source (resources/get-image :community-cover)
                          :style  {:flex               1
                                   :width              "100%"
                                   :border-radius      16}}]
            [react/blur-view {:flex               1
                              :style              {:border-radius      16
                                                   :height             100
                                                   :position        :absolute
                                                   :left            0
                                                   :right           0}
                              :blur-amount        20
                              :overlay-color      (colors/theme-colors
                                                   colors/white-opa-70
                                                   colors/neutral-80-opa-80)}]])
         [rn/view {:style {:position       :absolute
                           :align-self     :center}}
          [tag/tag (merge @state
                          {:id          1
                           :label       "Tag"
                           :labelled?    (if (= (:type @state) :label)
                                           true
                                           (:labelled? @state))
                           :resource    (if (= :emoji (:type @state))
                                          (resources/get-image :music)
                                          :main-icons2/placeholder)})]]]]])))
(defn preview-tag []
  [rn/view {:flex             1
            :background-color (colors/theme-colors
                               colors/white
                               colors/neutral-90)}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
