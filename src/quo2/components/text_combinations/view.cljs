(ns quo2.components.text-combinations.view
  (:require
    [quo2.theme :as theme]
    [quo2.components.markdown.text :as text]
    [quo2.components.text-combinations.style :as style]
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
           description-accessibility-label]}]
  [rn/view {:style container-style}
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
   (when description
     [text/text
      {:accessibility-label description-accessibility-label
       :weight              :regular
       :size                :paragraph-1
       :style               style/description-description-text}
      description])])

(def view (theme/with-theme view-internal))
