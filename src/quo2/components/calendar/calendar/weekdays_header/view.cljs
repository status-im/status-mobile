(ns quo2.components.calendar.calendar.weekdays-header.view
  (:require
    [quo2.components.calendar.calendar.weekdays-header.style :as style]
    [quo2.components.markdown.text :as text]
    [quo2.theme :as theme]
    [react-native.core :as rn]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]))

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
