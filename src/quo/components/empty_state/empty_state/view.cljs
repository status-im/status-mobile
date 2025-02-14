(ns quo.components.empty-state.empty-state.view
  (:require
    [quo.components.buttons.button.view :as button]
    [quo.components.empty-state.empty-state.styles :as styles]
    [quo.components.markdown.text :as text]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]))

(defn empty-state
  [{:keys        [customization-color image title description blur? placeholder? container-style]
    upper-button :upper-button
    lower-button :lower-button
    :or          {customization-color :blue}}]
  [rn/view {:style (merge styles/container container-style)}
   (if placeholder?
     [rn/view {:style styles/image-placeholder}]
     [rn/view {:style styles/image-container}
      [fast-image/fast-image
       {:style  styles/image
        :source image}]])
   [rn/view {:style styles/text-container}
    [text/text
     {:style           (styles/title blur?)
      :number-of-lines 1
      :weight          :semi-bold
      :size            :paragraph-1}
     title]
    [text/text
     {:style           (styles/description blur?)
      :number-of-lines 2
      :weight          :regular
      :size            :paragraph-2}
     description]]
   (when-let [{upper-button-text     :text
               upper-button-on-press :on-press} upper-button]
     [rn/view {:style styles/button-container}
      [button/button
       {:type                :primary
        :size                32
        :customization-color customization-color
        :on-press            upper-button-on-press}
       upper-button-text]
      (when-let [{lower-button-text     :text
                  lower-button-on-press :on-press} lower-button]
        [button/button
         {:container-style {:margin-top 12}
          :size            32
          :type            :grey
          :background      :blur
          :on-press        lower-button-on-press}
         lower-button-text])])])
