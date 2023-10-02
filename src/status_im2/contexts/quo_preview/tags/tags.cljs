(ns status-im2.contexts.quo-preview.tags.tags
  (:require [quo2.components.tags.tags :as tags]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im.ui.components.react :as react]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Size:"
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
   {:label "Scrollable:"
    :key   :scrollable?
    :type  :boolean}
   {:label   "Fade Out:"
    :key     :fade-end-percentage
    :type    :select
    :options [{:key   1
               :value "1%"}
              {:key   0.4
               :value "0.4%"}]}
   {:label "Labelled:"
    :key   :labelled?
    :type  :boolean}
   {:label "Disabled:"
    :key   :disabled?
    :type  :boolean}
   {:label "Blurred background:"
    :key   :blurred?
    :type  :boolean}])

(defn preview-tags
  []
  (let [state (reagent/atom {:size                32
                             :labelled?           true
                             :type                :emoji
                             :fade-end-percentage 0.4
                             :scrollable?         false})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view
        {:style {:padding-bottom 150
                 :padding-top    60}}
        [rn/view
         {:style {:flex               1
                  :justify-content    :center
                  :top                60
                  :padding-horizontal 16}}
         (when (:blurred? @state)
           [rn/view
            {:align-items   :center
             :height        100
             :border-radius 16}
            [react/image
             {:source (resources/get-mock-image :community-cover)
              :style  {:flex          1
                       :width         "100%"
                       :border-radius 16}}]
            [react/blur-view
             {:flex          1
              :style         {:border-radius 16
                              :height        100
                              :position      :absolute
                              :left          0
                              :right         0}
              :blur-amount   20
              :overlay-color (colors/theme-colors
                              colors/white-opa-70
                              colors/neutral-80-opa-80)}]])
         [rn/view
          {:style {:position   :absolute
                   :align-self :center}}
          [tags/tags
           (merge
            @state
            {:default-active 1
             :component      :tags
             :labelled?      (if (= :label (:type @state)) true (:labelled? @state))
             :data           [{:id 1 :label "Music" :resource (resources/get-image :music)}
                              {:id 2 :label "Lifestyle" :resource (resources/get-image :lifestyle)}
                              {:id 2 :label "Podcasts" :resource (resources/get-image :podcasts)}
                              {:id 2 :label "Music" :resource (resources/get-image :music)}
                              {:id 3 :label "Lifestyle" :resource (resources/get-image :lifestyle)}]}
            (when (:scrollable? @state)
              {:scroll-on-press? true
               :fade-end?        true}))]]]]])))
