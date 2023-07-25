(ns quo2.components.calendar.calendar.weekdays-header.view
  (:require [react-native.core :as rn]
            [quo2.theme :as theme]
            [utils.datetime :as dt]
            [utils.i18n :as i18n]
            [quo2.components.calendar.calendar.weekdays-header.style :as style]))

(defn- weekdays-header-internal
  []
  [rn/view
   {:style style/container-weekday-row}
   (doall
    (for [name dt/weekday-names]
      [rn/view
       {:style style/container-weekday
        :key   name}
       [rn/text
        {:style (style/text-weekdays)}
        (str (i18n/label name))]]))])

(def weekdays-header (theme/with-theme weekdays-header-internal))
