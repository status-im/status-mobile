(ns quo2.components.calendar.calendar.weekdays-header.view
  (:require [react-native.core :as rn]
            [quo2.theme :as theme]
            [utils.datetime :as datetime]
            [utils.i18n :as i18n]
            [quo2.components.markdown.text :as text]
            [quo2.components.calendar.calendar.weekdays-header.style :as style]))

(defn- view-internal
  [theme]
  [rn/view
   {:style style/container-weekday-row}
   (for [weekday datetime/weekday-names]
     [rn/view
      {:style style/container-weekday
       :key   weekday}
      [text/text
       {:weight :medium
        :size   :paragraph-2
        :style  (style/text-weekdays theme)}
       (str (i18n/label weekday))]])])

(def view (theme/with-theme view-internal))
