(ns quo.components.text-combinations.view
  (:require
    [quo.components.buttons.button.view :as button]
    [quo.components.markdown.text :as text]
    [quo.components.text-combinations.style :as style]
    [quo.theme :as theme]
    [react-native.core :as rn]))

(defn icon
  [source size emoji? emoji-background-color]
  (if emoji?
    [rn/view
     {:style {:border-radius    50
              :border-width     0
              :border-color     :transparent
              :width            size
              :height           size
              :justify-content  :center
              :align-items      :center
              :background-color emoji-background-color}}
     [text/text
      {:style {:margin-left 1
               :margin-top  1}}
      source]]
    [rn/image
     {:source (if (string? source)
                {:uri source}
                source)
      :style  {:border-radius 50
               :border-width  0
               :border-color  :transparent
               :width         size
               :height        size}}]))

(defn view-internal
  [{:keys [container-style
           title
           title-number-of-lines
           avatar
           title-accessibility-label
           description
           description-accessibility-label
           button-icon
           button-on-press
           customization-color
<<<<<<< HEAD
           emoji-hash]
    :or   {title-number-of-lines 1}}]
=======
           emoji-hash
           emoji
           emoji-background-color]}]
>>>>>>> 5c0581223 (Improvements)
  [rn/view {:style container-style}
   [rn/view
    {:style {:flex-direction  :row
             :justify-content :space-between}}
    [rn/view {:style style/title-container}
     (when avatar
       [rn/view {:style style/avatar-container}
        [icon avatar 32 nil nil]])
     (when emoji
       [rn/view {:style style/avatar-container}
        [icon emoji 32 true emoji-background-color]])
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
      emoji-hash])])

(def view (theme/with-theme view-internal))
