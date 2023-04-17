(ns status-im2.contexts.quo-preview.tabs.segmented-tab
  (:require [quo2.components.tabs.segmented-tab :as quo2]
            [quo2.foundations.colors :as colors]
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

(defn cool-preview
  []
  (let [state (reagent/atom {:size  32
                             :blur? false})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
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

(defn preview-segmented
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
