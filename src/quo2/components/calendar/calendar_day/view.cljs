(ns quo2.components.calendar.calendar-day.view
  (:require [react-native.core :as rn]
            [quo2.theme :as theme]
            [quo2.components.calendar.calendar-day.style :as style]))

(defn- calendar-day-internal
  [{:keys [state in-range on-press] :or {state :default}} day]
  [rn/view
   {:style style/wrapper}
   [rn/view {:style (style/in-range-background {:in-range in-range})}]
   [rn/touchable-opacity
    {:on-press on-press
     :style    (style/container
                {:state state})
     :disabled (= state :disabled)}
    [rn/text
     {:style (style/text {:state state})}
     day]
    [rn/view
     {:style (style/indicator {:state state})}]]])

(def calendar-day (theme/with-theme calendar-day-internal))
