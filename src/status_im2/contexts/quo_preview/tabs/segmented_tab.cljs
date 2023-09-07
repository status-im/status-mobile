(ns status-im2.contexts.quo-preview.tabs.segmented-tab
  (:require [quo2.components.tabs.segmented-tab :as quo2]
            [quo2.theme :as theme]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Size:"
    :key     :size
    :type    :select
    :options [{:key   28
               :value "28"}
              {:key   20
               :value "20"}]}
   {:label "Blur?"
    :key   :blur?
    :type  :boolean}])

(defn preview-segmented
  []
  (let [state (reagent/atom {:size  32
                             :blur? false})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view {:padding-bottom 150}
        [preview/blur-view
         {:show-blur-background? (:blur? @state)
          :height                200
          :style                 {:width "100%"}
          :blur-view-props       {:blur-type (theme/get-theme)}}
         [:<>
          [quo2/segmented-control
           (merge @state
                  {:default-active 1
                   :data           [{:id 1 :label "Tab 1"}
                                    {:id 2 :label "Tab 2"}
                                    {:id 3 :label "Tab 3"}
                                    {:id 4 :label "Tab 4"}]
                   :on-change      #(println "Active tab" %)})]
          [rn/view {:style {:padding-top 24}}
           [quo2/segmented-control
            (merge @state
                   {:default-active 1
                    :data           [{:id 1 :label "Tab 1"}
                                     {:id 2 :label "Tab 2"}]
                    :on-change      #(println "Active tab" %)})]]]]]])))
