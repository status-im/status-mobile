(ns quo.components.switchers.card-main-info.view
  (:require
    [quo.components.markdown.text :as text]
    [quo.components.switchers.card-main-info.style :as style]
    [react-native.core :as rn]))

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
