(ns quo2.components.calendar.calendar.days-grid.view
  (:require [react-native.core :as rn]
            [cljs-time.core :as time]
            [quo2.components.calendar.calendar.days-grid.utils :as utils]
            [quo2.components.calendar.calendar-day.view :as calendar-day]
            [quo2.components.calendar.calendar.days-grid.style :as style]))

(defn- day-view
  [day _ _ {:keys [year month selection-range on-press customization-color]}]
  (let [today      (time/now)
        start-date (:start-date selection-range)
        end-date   (:end-date selection-range)
        state      (utils/get-day-state day today year month start-date end-date)
        in-range   (utils/get-in-range-pos day start-date end-date)
        on-press   #(on-press (time/date-time day))]
    [calendar-day/view
     {:customization-color customization-color
      :state               state
      :in-range            in-range
      :on-press            on-press}
     (str (time/day day))]))

(defn view
  [{:keys [year month on-change start-date end-date customization-color]}]
  (let [on-day-press (fn [day]
                       (let [new-selection (utils/update-range day start-date end-date)]
                         (on-change new-selection)))]
    [rn/view
     {:style style/container-days}
     [rn/flat-list
      {:data                    (utils/day-grid year month)
       :key-fn                  str
       :num-columns             7
       :content-container-style {:margin-horizontal -2}
       :render-fn               day-view
       :render-data             {:customization-color customization-color
                                 :year                year
                                 :month               month
                                 :on-press            on-day-press
                                 :selection-range     {:start-date start-date
                                                       :end-date   end-date}}}]]))
