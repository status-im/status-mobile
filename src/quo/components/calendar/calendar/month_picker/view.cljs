(ns quo.components.calendar.calendar.month-picker.view
  (:require
    [quo.components.buttons.button.view :as button]
    [quo.components.calendar.calendar.month-picker.style :as style]
    [quo.components.calendar.calendar.month-picker.utils :as utils]
    [quo.components.markdown.text :as text]
    [quo.theme :as theme]
    [react-native.core :as rn]
    [utils.number :as utils.number]))

(defn- view-internal
  [{:keys [year month on-change theme]}]
  (let [year  (utils.number/parse-int year)
        month (utils.number/parse-int month)]
    [rn/view
     {:style style/container}
     [button/button
      {:icon                true
       :type                :outline
       :accessibility-label :previous-month-button
       :size                24
       :on-press            #(on-change (utils/previous-month year month))}
      :i/chevron-left]
     [text/text
      {:weight :semi-bold
       :size   :paragraph-1
       :style  (style/text theme)}
      (utils/format-month-year year month)]
     [button/button
      {:icon                true
       :accessibility-label :next-month-button
       :size                24
       :type                :outline
       :on-press            #(on-change (utils/next-month year month))}
      :i/chevron-right]]))

(def view (theme/with-theme view-internal))
