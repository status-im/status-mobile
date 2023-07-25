(ns quo2.components.calendar.calendar.days-grid.view
  (:require [react-native.core :as rn]
            [quo2.theme :as theme]
            [cljs-time.core :as t]
            [quo2.components.calendar.calendar.days-grid.utils :as utils]
            [quo2.components.calendar.calendar-day.view :as calendar-day]
            [quo2.components.calendar.calendar.days-grid.style :as style]))

(defn render-day
  [{:keys [year month day selection-range on-press]}]
  (let [today      (t/now)
        start-date (:start-date selection-range)
        end-date   (:end-date selection-range)
        state      (utils/get-day-state day today year month start-date end-date)
        in-range   (utils/get-in-range-pos day start-date end-date)
        on-press   #(on-press (t/date-time day))]
    [calendar-day/calendar-day
     {:state    state
      :in-range in-range
      :on-press on-press}
     (str (t/day day))]))

(defn- days-grid-internal
  [{:keys [year month on-change start-date end-date]}]
  (let [on-day-press (fn [day]
                       (let [new-selection (utils/update-range day start-date end-date)]
                         (on-change new-selection)))]
    [rn/view
     {:style style/container-days}
     [rn/flat-list
      {:data                    (utils/day-grid year month)
       :key-fn                  str
       :numColumns              7
       :content-container-style {:margin-horizontal -2}
       :render-fn               (fn [item]
                                  (render-day
                                   {:year            year
                                    :month           month
                                    :day             item
                                    :on-press        on-day-press
                                    :selection-range {:start-date start-date :end-date end-date}}))}]]))

(def days-grid (theme/with-theme days-grid-internal))
