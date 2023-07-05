(ns stories.counter.counter-story
  (:require
   [react-native.core :as rn]
   [quo2.components.counter.counter :as counter]
   [reagent.core :as reagent]
   [quo2.foundations.colors :as colors]
   [quo2.theme :as theme]))

(def ^:export default
  #js {:title "Counter Stories"
       :component counter/counter})

(def counter-configs
  [[{:customization-color :blue} 5]
   [{:type :secondary} 5]
   [{:type :grey} 5]
   [{:type :outline} 5]
   [{:customization-color :blue} 10]
   [{:type :secondary} 10]
   [{:type :grey} 10]
   [{:type :outline} 10]
   [{:customization-color :blue} 100]
   [{:type :secondary} 100]
   [{:type :grey} 100]
   [{:type :outline} 100]])

(defn ^:export Counter
  []
  (reagent/as-element [:<>
                       [rn/view {:style {:flex 1
                                         :width 200
                                         :flex-direction :row}}
                        [theme/provider {:theme :light}
                         [rn/view  {:style {:flex 1
                                            :align-items :center
                                            :border-radius 16
                                            :padding-top 26}}
                          (map (fn [[props children]]
                                 [rn/view
                                  {:style {:margin-bottom 28}}
                                  [counter/counter props children]])
                               counter-configs)]]

                        [theme/provider {:theme :dark}
                         [rn/view {:style {:flex 1
                                           :align-items :center
                                           :border-radius 16
                                           :padding-top 26
                                           :background-color colors/neutral-95}}
                          (map (fn [[props children]]
                                 [rn/view
                                  {:style {:margin-bottom 28}}
                                  [counter/counter props children]])
                               counter-configs)]]]]))



