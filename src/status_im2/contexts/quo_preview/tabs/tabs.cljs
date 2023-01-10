(ns status-im2.contexts.quo-preview.tabs.tabs
  (:require [quo2.components.tabs.tabs :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Size:"
    :key     :size
    :type    :select
    :options [{:key   32
               :value "32"}
              {:key   24
               :value "24"}]}
   {:label "Scrollable:"
    :key   :scrollable?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:size        32
                             :scrollable? false})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
        [rn/view
         {:padding-vertical 60
          :flex-direction   :row
          :justify-content  :center}
         [quo2/tabs
          (merge @state
                 {:default-active 1
                  :data           [{:id 1 :label "Tab 1"}
                                   {:id 2 :label "Tab 2"}
                                   {:id 3 :label "Tab 3"}
                                   {:id 4 :label "Tab 4"}]
                  :on-change      #(println "Active tab" %)}
                 (when (:scrollable? @state)
                   {:scroll-on-press?    true
                    :fade-end-percentage 0.4
                    :fade-end?           true}))]]]])))

(defn preview-tabs
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                      1
     :keyboardShouldPersistTaps :always
     :header                    [cool-preview]
     :key-fn                    str}]])
