(ns quo2.components.empty-state.empty-state.view
  (:require [quo2.components.buttons.button :as button]
            [quo2.components.empty-state.empty-state.styles :as styles]
            [quo2.components.markdown.text :as text]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]))

(defn empty-state
  [{:keys        [customization-color image title description blur?]
    upper-button :upper-button
    lower-button :lower-button
    :or          {customization-color :blue}}]
  [rn/view {:style styles/container}
   [fast-image/fast-image
    {:style  styles/image
     :source image}]
   [rn/view {:style styles/text-container}
    [text/text
     {:style           (styles/title blur?)
      :number-of-lines 1
      :weight          :semi-bold
      :size            :paragraph-1}
     title]
    [text/text
     {:style           (styles/description blur?)
      :number-of-lines 1
      :text-align      :center
      :weight          :regular
      :size            :paragraph-2}
     description]]
   (when-let [{upper-button-text     :text
               upper-button-on-press :on-press} upper-button]
     [rn/view {:style styles/button-container}
      [button/button
       (cond-> {:type                      :primary
                :override-background-color (styles/upper-button-color customization-color)
                :on-press                  upper-button-on-press}
         blur? (assoc :override-theme :dark))
       upper-button-text]

      (when-let [{lower-button-text     :text
                  lower-button-on-press :on-press} lower-button]
        [button/button
         (cond-> {:style    {:margin-top 12}
                  :type     :blur-bg
                  :on-press lower-button-on-press}
           blur? (assoc :override-theme :dark))
         lower-button-text])])])
