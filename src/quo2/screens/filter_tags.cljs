(ns quo2.screens.filter-tags
  (:require [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [status-im.ui.components.react :as react]
            [quo2.foundations.colors :as colors]
            [quo2.components.filter-tags :as quo2.tags]
            [status-im.react-native.resources :as resources]
            [status-im.utils.handlers :refer [<sub]]
            [reagent.core :as reagent]))

(def descriptor [{:label   "Size:"
                  :key     :size
                  :type    :select
                  :options [{:key   32
                             :value "32"}
                            {:key   24
                             :value "24"}]}
                 {:label   "Label:"
                  :key     :label
                  :type    :select
                  :options [{:key   true
                             :value "with tag"}
                            {:key   false
                             :value "no tag"}]}
                 {:label   "Type:"
                  :key     :type
                  :type    :select
                  :options [{:key   :emoji
                             :value "emoji"}
                            {:key   :icon
                             :value "icons"}
                            {:key   :text
                             :value "text"}]}
                 {:label "Disabled:"
                  :key   :disabled
                  :type  :boolean}
                 {:label "Blurred background:"
                  :key   :blurred
                  :type  :boolean}])

(defn cool-preview []
  (let [state  (reagent/atom {:size       32
                              :label      true
                              :type       :emoji})]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [rn/view {:flex 1}
        [preview/customizer state descriptor]]
       [rn/view {:justify-content   :center
                 :top               60}
        (when (:blurred @state)
          [rn/view {:position       :absolute}
           [react/image {:source (resources/get-image :test-bg-image)
                         :style  {:width              (* (<sub [:dimensions/window-width]) 0.91)
                                  :border-radius      16}}]
           [react/blur-view {:blur-amount        40
                             :border-radius      16
                             :width              :100%
                             :height             :100%
                             :position           :absolute
                             :overlay-color      (colors/theme-colors
                                                  colors/white-opa-70
                                                  colors/neutral-80-opa-80)}]])
        [rn/view {:align-items        :center
                  :justify-content    :center}
         [quo2.tags/tags (merge @state
                                {:default-active 1
                                 :data           [{:id 1 :tag-label "Music" :emoji (resources/get-image :music)
                                                   :icon :main-icons2/placeholder}
                                                  {:id 2 :tag-label "Lifestyle" :emoji (resources/get-image :lifestyle)
                                                   :icon :main-icons2/placeholder}
                                                  {:id 3 :tag-label "Podcasts" :emoji (resources/get-image :podcasts)
                                                   :icon :main-icons2/placeholder}]})]]]])))
(defn preview-tags []
  [rn/view {:flex             1
            :background-color (colors/theme-colors
                               colors/neutral-5
                               colors/neutral-90)}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])