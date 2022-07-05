(ns quo2.screens.filter-tags
  (:require [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [quo2.foundations.colors :as colors]
            [quo2.components.filter-tags :as quo2.tags]
            [status-im.react-native.resources :as resources]
            [reagent.core :as reagent]))

(def descriptor [{:label   "Size:"
                  :key     :size
                  :type    :select
                  :options [{:key   32
                             :value "32"}
                            {:key   24
                             :value "24"}]}
                 {:label   "Label:"
                  :key     :with-label
                  :type    :select
                  :options [{:key   true
                             :value "with label"}
                            {:key   false
                             :value "no label"}]}
                 {:label   "Type:"
                  :key     :type
                  :type    :select
                  :options [{:key   :emoji
                             :value "emoji"}
                            {:key   :icon
                             :value "icons"}]}])

(defn cool-preview []
  (let [state  (reagent/atom {:size  32
                              :with-label true
                              :type :emoji})]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [rn/view {:flex 1}
        [preview/customizer state descriptor]]
       [rn/view {:padding-vertical  60
                 :flex-direction    :row
                 :justify-content   :center}
        [quo2.tags/tags (merge @state
                               {:data      [{:id 1 :label "Music" :emoji (resources/get-image :music)
                                             :icon :main-icons2/placeholder}
                                            {:id 2 :label "Lifestyle"    :emoji (resources/get-image :lifestyle)
                                             :icon :main-icons2/placeholder}
                                            {:id 4 :label "Podcasts"   :emoji (resources/get-image :podcasts)
                                             :icon :main-icons2/placeholder}]})]]])))

(defn preview-tags []
  [rn/view {:background-color (:ui-background (colors/theme-colors
                                               colors/white
                                               colors/neutral-90))
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])