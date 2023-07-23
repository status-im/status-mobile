(ns quo2.components.calendar.calendar-year.view
  (:require [react-native.core :as rn]
            [quo2.theme :as theme]
            [quo2.components.calendar.calendar-year.style :as style]))

(defn- calendar-year-internal
  [{:keys [selected? disabled? on-press]} year]
  [rn/touchable-opacity
   {:on-press on-press
    :style    (style/container
               {:selected? selected?
                :disabled? disabled?})
    :disabled disabled?}
   [rn/text
    {:style (style/text {:selected? selected?})}
    year]])


(def calendar-year (theme/with-theme calendar-year-internal))
