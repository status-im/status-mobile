(ns quo2.components.calendar.calendar.years-list.view
  (:require [react-native.core :as rn]
            [quo2.theme :as theme]
            [react-native.linear-gradient :as linear-gradient]
            [quo2.components.calendar.calendar.utils :as utils]
            [quo2.components.calendar.calendar-year.view :as calendar-year]
            [quo2.components.calendar.calendar.years-list.style :as style]))

(defn- year-view
  [year _ _ {:keys [selected-year on-press]}]
  [calendar-year/view
   {:selected? (= year selected-year)
    :on-press  #(on-press year)}
   (str year)])

(defn- separator
  []
  [rn/view {:style {:height 4}}])

(defn- footer
  []
  [rn/view {:style {:height 32}}])

(defn- gradiant-overview
  [theme]
  [linear-gradient/linear-gradient
   {:colors [(style/gradient-start-color theme) (style/gradient-end-color theme)]
    :style  style/gradient-view
    :start  {:x 0 :y 0}
    :end    {:x 0 :y 1}}])

(defn view-internal
  [{:keys [on-change-year year theme]}]
  [rn/view
   {:style (style/container-years theme)}
   [rn/flat-list
    {:data                            (utils/generate-years (utils/current-year))
     :key-fn                          str
     :list-key                        :years-list
     :inverted                        true
     :shows-vertical-scroll-indicator false
     :footer                          [footer]
     :separator                       [separator]
     :render-fn                       year-view
     :render-data                     {:selected-year year
                                       :on-press      #(on-change-year %)}}]
   [gradiant-overview theme]])

(def view (theme/with-theme view-internal))
