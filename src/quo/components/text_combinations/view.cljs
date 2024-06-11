(ns quo.components.text-combinations.view
  (:require
    [quo.components.buttons.button.view :as button]
    [quo.components.markdown.text :as text]
    [quo.components.text-combinations.style :as style]
    [quo.theme]
    [react-native.core :as rn]))

(defn icon
  [{:keys [source size customization-color theme]}]
  (if customization-color
    [rn/view {:style (style/textual-emoji size customization-color theme)}
     [text/text
      source]]
    [rn/image
     {:source source
      :style  {:border-radius 50
               :border-width  0
               :border-color  :transparent
               :width         size
               :height        size}}]))

(defn view
  [{:keys [container-style title title-number-of-lines avatar title-accessibility-label description emoji
           description-accessibility-label button-icon button-on-press customization-color emoji-hash]
    :or   {title-number-of-lines 1}}]
  (let [theme (quo.theme/use-theme)]
    [rn/view {:style container-style}
     [rn/view
      {:style {:flex-direction  :row
               :justify-content :space-between}}
      [rn/view {:style style/title-container}
       (when avatar
         [rn/view {:style style/avatar-container}
          [icon {:source avatar :size 32}]])
       (when emoji
         [rn/view {:style style/avatar-container}
          [icon {:source emoji :size 32 :customization-color customization-color :theme theme}]])
       [text/text
        {:accessibility-label title-accessibility-label
         :weight              :semi-bold
         :ellipsize-mode      :tail
         :style               {:flex 1}
         :number-of-lines     title-number-of-lines
         :size                :heading-1}
        title]]
      (when button-icon
        [button/button
         {:icon-only?          true
          :on-press            button-on-press
          :customization-color customization-color
          :size                32} button-icon])]
     (when description
       [text/text
        {:accessibility-label description-accessibility-label
         :weight              :regular
         :size                :paragraph-1
         :style               style/description-description-text}
        description])
     (when emoji-hash
       [text/text
        {:number-of-lines     1
         :accessibility-label :emoji-hash
         :style               style/emoji-hash}
        emoji-hash])]))
