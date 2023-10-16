(ns quo2.components.calendar.calendar.month-picker.view
  (:require
    [quo2.components.buttons.button.view :as button]
    [quo2.components.calendar.calendar.month-picker.style :as style]
    [quo2.components.calendar.calendar.month-picker.utils :as utils]
    [quo2.components.markdown.text :as text]
    [quo2.theme :as theme]
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
