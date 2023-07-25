(ns quo2.components.calendar.calendar.years-list.view
  (:require [react-native.core :as rn]
            [quo2.theme :as theme]
            [reagent.core :as reagent]
            [quo2.foundations.colors :as colors]
            [react-native.linear-gradient :as linear-gradient]
            [quo2.components.calendar.calendar.utils :as utils]
            [quo2.components.calendar.calendar-year.view :as calendar-year]
            [quo2.components.calendar.calendar.years-list.style :as style]))

(defn render-year
  [year selected-year on-press]
  [calendar-year/calendar-year
   {:selected? (= year selected-year)
    :on-press  #(on-press year)}
   (str year)])

(defn on-year-press
  [on-change selected-year]
  (do
    #(reset! selected-year %)
    (on-change selected-year)))

(defn- separator
  []
  [rn/view {:style {:height 4}}])

(defn- footer
  []
  [rn/view {:style {:height 32}}])

(defn years-list-view-internal
  [{:keys [on-change-year year]}]
  (let [selected-year (reagent/atom year)]
    [rn/view
     {:style (style/container-years)}
     [rn/flat-list
      {:data                            (utils/generate-years (utils/current-year))
       :key-fn                          str
       :listKey                         :years-list
       :inverted                        true
       :shows-vertical-scroll-indicator false
       :footer                          (footer)
       :separator                       (separator)
       :render-fn                       (fn [item]
                                          (render-year item
                                                       @selected-year
                                                       #(on-year-press on-change-year item)))}]
     [linear-gradient/linear-gradient
      {:colors [(style/gradient-start-color) (style/gradient-end-color)]
       :style  style/gradient-view
       :start  {:x 0 :y 0}
       :end    {:x 0 :y 1}}]]))

(def years-list-view (theme/with-theme years-list-view-internal))
