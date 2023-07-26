(ns quo2.components.calendar.calendar-day.view
  (:require [react-native.core :as rn]
            [quo2.theme :as theme]
            [quo2.components.markdown.text :as text]
            [quo2.components.calendar.calendar-day.style :as style]))

(defn- calendar-day-internal
  [{:keys [state in-range on-press customization-color theme]
    :or   {state :default}}
   day]
  [rn/view
   {:style style/wrapper}
   [rn/view {:style (style/in-range-background {:in-range in-range :theme theme})}]
   [rn/touchable-opacity
    {:on-press on-press
     :style    (style/container
                {:state               state
                 :theme               theme
                 :customization-color customization-color})
     :disabled (= state :disabled)}
    [text/text
     {:weight :medium
      :size   :paragraph-2
      :style  (style/text {:state state :theme theme})}
     day]
    [rn/view
     {:style (style/indicator
              {:state               state
               :theme               theme
               :customization-color customization-color})}]]])

(def calendar-day (theme/with-theme calendar-day-internal))
