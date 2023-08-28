(ns quo2.components.text-combinations.title.view
  (:require
    [quo2.components.markdown.text :as text]
    [quo2.components.text-combinations.title.style :as style]
    [react-native.core :as rn]))

(defn title
  [{:keys [container-style
           title
           title-accessibility-label
           subtitle
           subtitle-accessibility-label]}]
  [rn/view {:style (style/title-container container-style)}
   [text/text
    {:accessibility-label title-accessibility-label
     :weight              :semi-bold
     :size                :heading-1
     :style               style/title-text}
    title]
   (when subtitle
     [text/text
      {:accessibility-label subtitle-accessibility-label
       :weight              :regular
       :size                :paragraph-1
       :style               style/subtitle-text}
      subtitle])])
