(ns status-im.contexts.preview-screens.quo-preview.tabs.segmented-tab
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :size
    :type    :select
    :options [{:key   28
               :value "28"}
              {:key   20
               :value "20"}]}
   {:key  :blur?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:size  32
                             :blur? false})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :blur?                     (:blur? @state)
        :show-blur-background?     true
        :component-container-style {:padding-vertical 60}}
       [:<>
        [quo/segmented-control
         (assoc @state
                :default-active 1
                :data           [{:id 1 :label "Tab 1"}
                                 {:id 2 :label "Tab 2"}
                                 {:id 3 :label "Tab 3"}
                                 {:id 4 :label "Tab 4"}]
                :on-change      #(println "Active tab" %))]
        [rn/view {:style {:padding-top 24}}
         [quo/segmented-control
          (assoc @state
                 :default-active 1
                 :data           [{:id 1 :label "Tab 1"}
                                  {:id 2 :label "Tab 2"}]
                 :on-change      #(println "Active tab" %))]]]])))
