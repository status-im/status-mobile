(ns quo.components.calendar.calendar-year.view
  (:require
    [quo.components.calendar.calendar-year.style :as style]
    [quo.components.markdown.text :as text]
    [quo.theme :as theme]
    [react-native.core :as rn]))

(defn- view-internal
  [{:keys [selected? disabled? on-press theme]} year]
  [rn/touchable-opacity
   {:on-press on-press
    :style    (style/container
               {:selected? selected?
                :disabled? disabled?
                :theme     theme})
    :disabled disabled?}
   [text/text
    {:weight :medium
     :size   :paragraph-2
     :style  (style/text
              {:selected? selected?
               :theme     theme})}
    year]])

(def view (theme/with-theme view-internal))
