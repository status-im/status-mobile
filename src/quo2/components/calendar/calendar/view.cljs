(ns quo2.components.calendar.calendar.view
  (:require [react-native.core :as rn]
            [quo2.theme :as theme]
            [reagent.core :as reagent]
            [utils.number :as utils.number]
            [quo2.components.calendar.calendar.utils :as utils]
            [quo2.components.calendar.calendar.style :as style]
            [quo2.components.calendar.calendar.years-list.view :as years-list]
            [quo2.components.calendar.calendar.days-grid.view :as days-grid]
            [quo2.components.calendar.calendar.weekdays-header.view :as weekdays-header]
            [quo2.components.calendar.calendar.month-picker.view :as month-picker]))

(defn- view-internal
  []
  (let [selected-year   (reagent/atom (utils/current-year))
        selected-month  (reagent/atom (utils/current-month))
        on-change-year  #(reset! selected-year %)
        on-change-month (fn [new-date]
                          (reset! selected-year (utils.number/parse-int (:year new-date)))
                          (reset! selected-month (utils.number/parse-int (:month new-date))))]
    (fn [{:keys [on-change start-date end-date theme]}]
      [rn/view
       {:style (style/container theme)}
       [years-list/view
        {:on-change-year on-change-year
         :year           @selected-year}]
       [rn/view
        {:style style/container-main}
        [month-picker/view
         {:year      @selected-year
          :month     @selected-month
          :on-change on-change-month}]
        [weekdays-header/view]
        [days-grid/view
         {:year                @selected-year
          :month               @selected-month
          :start-date          start-date
          :end-date            end-date
          :on-change           on-change
          :customization-color :blue}]]])))

(def view (theme/with-theme view-internal))
