(ns quo.components.calendar.calendar-day.view
  (:require
    [quo.components.calendar.calendar-day.style :as style]
    [quo.components.markdown.text :as text]
    [quo.context]
    [react-native.core :as rn]))

(defn view
  [{:keys [state in-range on-press customization-color]
    :or   {state :default}}
   day]
  (let [theme (quo.context/use-theme)]
    [rn/view {:style style/wrapper}
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
                 :customization-color customization-color})}]]]))
