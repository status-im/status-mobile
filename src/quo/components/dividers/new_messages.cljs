(ns quo.components.dividers.new-messages
  (:require
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.linear-gradient :as linear-gradient]))

(defn view
  "new-messages params - label, customization-color, theme"
  [{:keys [label customization-color] :or {customization-color :blue}}]
  (let [theme      (quo.theme/use-theme)
        bg-color   (colors/resolve-color customization-color theme 5)
        text-color (colors/resolve-color customization-color theme)]
    [linear-gradient/linear-gradient
     {:colors [bg-color "rgba(0,0,0,0)"]
      :start  {:x 0 :y 0}
      :end    {:x 0 :y 1}}
     [rn/view
      {:style {:padding-left     60
               :padding-vertical 12
               :padding-right    24}}
      [text/text
       {:size   :paragraph-2
        :weight :medium
        :style  {:color text-color}}
       label]]]))
