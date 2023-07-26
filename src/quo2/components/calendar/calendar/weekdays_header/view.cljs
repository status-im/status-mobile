(ns quo2.components.calendar.calendar.weekdays-header.view
  (:require [react-native.core :as rn]
            [quo2.theme :as theme]
            [utils.datetime :as dt]
            [utils.i18n :as i18n]
            [quo2.components.markdown.text :as text]
            [quo2.components.calendar.calendar.weekdays-header.style :as style]))

(defn- weekdays-header-internal
  [theme]
  [rn/view
   {:style style/container-weekday-row}
   (doall
    (for [name dt/weekday-names]
      [rn/view
       {:style style/container-weekday
        :key   name}
       [text/text
        {:weight :medium
         :size   :paragraph-2
         :style  (style/text-weekdays theme)}
        (str (i18n/label name))]]))])

(def weekdays-header (theme/with-theme weekdays-header-internal))
