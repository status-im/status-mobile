(ns quo2.components.switchers.card-main-info.view
  (:require [quo2.components.markdown.text :as text]
            [quo2.components.switchers.card-main-info.style :as style]
            [quo.react-native :as rn]))

(defn view
  [{:keys [title subtitle]}]
  [rn/view
   [text/text
    {:accessibility-label :title
     :size                :paragraph-1
     :weight              :semi-bold
     :number-of-lines     1
     :ellipsize-mode      :tail}
    title]
   [text/text
    {:accessibility-label :subtitle
     :size                :paragraph-2
     :weight              :medium
     :style               style/subtitle}
    subtitle]])
