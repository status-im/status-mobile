(ns quo.components.calendar.calendar.view
  (:require
    [quo.components.calendar.calendar.days-grid.view :as days-grid]
    [quo.components.calendar.calendar.month-picker.view :as month-picker]
    [quo.components.calendar.calendar.style :as style]
    [quo.components.calendar.calendar.utils :as utils]
    [quo.components.calendar.calendar.weekdays-header.view :as weekdays-header]
    [quo.components.calendar.calendar.years-list.view :as years-list]
    [quo.theme]
    [react-native.core :as rn]
    [utils.number :as utils.number]))

(defn view
  [{:keys [on-change start-date end-date]}]
  (let [theme                               (quo.theme/use-theme)
        [selected-year set-selected-year]   (rn/use-state (utils/current-year))
        [selected-month set-selected-month] (rn/use-state (utils/current-month))
        on-change-year                      (rn/use-callback #(set-selected-year %))
        on-change-month                     (rn/use-callback
                                             (fn [new-date]
                                               (set-selected-year
                                                (utils.number/parse-int (:year new-date)))
                                               (set-selected-month
                                                (utils.number/parse-int (:month new-date)))))]
    [rn/view
     {:style (style/container theme)}
     [years-list/view
      {:on-change-year on-change-year
       :year           selected-year}]
     [rn/view
      {:style style/container-main}
      [month-picker/view
       {:year      selected-year
        :month     selected-month
        :on-change on-change-month}]
      [weekdays-header/view]
      [days-grid/view
       {:year                selected-year
        :month               selected-month
        :start-date          start-date
        :end-date            end-date
        :on-change           on-change
        :customization-color :blue}]]]))
