(ns quo2.screens.tabs.tabs
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [quo.previews.preview :as preview]
            [quo2.components.tabs.tabs :as quo2]
            [reagent.core :as reagent]))

(def descriptor [{:label   "Size:"
                  :key     :size
                  :type    :select
                  :options [{:key   32
                             :value "32"}
                            {:key   24
                             :value "24"}]}])

(defn cool-preview []
  (let [state  (reagent/atom {:size  32})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
        [rn/view {:padding-vertical 60
                  :flex-direction   :row
                  :justify-content  :center}
         [quo2/tabs (merge @state
                           {:default-active 1
                            :data [{:id 1 :label "Tab 1"}
                                   {:id 2 :label "Tab 2"}
                                   {:id 3 :label "Tab 3"}
                                   {:id 4 :label "Tab 4"}]
                            :on-change #(println "Active tab" %)})]]]])))

(defn preview-tabs []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
