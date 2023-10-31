(ns quo.components.text-combinations.view
  (:require
    [quo.components.buttons.button.view :as button]
    [quo.components.markdown.text :as text]
    [quo.components.text-combinations.style :as style]
    [quo.theme :as theme]
    [react-native.core :as rn]))

(defn icon
  [source size]
  [rn/image
   {:source (if (string? source)
              {:uri source}
              source)
    :style  {:border-radius 50
             :border-width  0
             :border-color  :transparent
             :width         size
             :height        size}}])

(defn view-internal
  [{:keys [container-style
           title
           avatar
           title-accessibility-label
           description
           description-accessibility-label
           button-icon
           button-on-press]}]
  [rn/view {:style container-style}
   [rn/view
    {:style {:flex-direction  :row
             :justify-content :space-between}}
    [rn/view {:style style/title-container}
     (when avatar
       [rn/view {:style style/avatar-container}
        [icon avatar 32]])
     [text/text
      {:accessibility-label title-accessibility-label
       :weight              :semi-bold
       :ellipsize-mode      :tail
       :number-of-lines     1
       :size                :heading-1}
      title]]
    (when button-icon
      [button/button
       {:icon-only? true
        :on-press   button-on-press
        :size       32} button-icon])]
   (when description
     [text/text
      {:accessibility-label description-accessibility-label
       :weight              :regular
       :size                :paragraph-1
       :style               style/description-description-text}
      description])])

(def view (theme/with-theme view-internal))
