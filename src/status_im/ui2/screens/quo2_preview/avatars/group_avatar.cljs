(ns status-im.ui2.screens.quo2-preview.avatars.group-avatar
  (:require [reagent.core :as reagent]
            [react-native.core :as rn]
            [status-im.ui2.screens.quo2-preview.preview :as preview]
            [quo2.foundations.colors :as colors]
            [quo2.components.avatars.group-avatar :as quo2]))

(def descriptor [{:label "Size"
                  :key :size
                  :type :select
                  :options [{:key :small
                             :value "Small"}
                            {:key :medium
                             :value "Medium"}
                            {:key :large
                             :value "Large"}]}
                 {:label "Color"
                  :key   :color
                  :type  :select
                  :options
                  (map
                   (fn [c]
                     {:key   c
                      :value c})
                   (keys colors/customization))}])

(defn cool-preview []
  (let [state (reagent/atom {:theme :light
                             :color :purple
                             :size :small})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
        [rn/view {:padding-vertical 60
                  :flex-direction   :row
                  :justify-content  :center}
         [quo2/group-avatar @state]]]])))

(defn preview-group-avatar []
  [rn/view {:background-color (colors/theme-colors colors/white
                                                   colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
