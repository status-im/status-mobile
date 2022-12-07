(ns quo2.components.dividers.new-messages
  (:require [react-native.core :as rn]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.linear-gradient :as linear-gradient]))

(defn new-messages
  "new-messages params - label, color"
  [{:keys [label color] :or {color :primary}}]
  (let [bg-color   (colors/custom-color-by-theme color 50 60 5 5)
        text-color (colors/custom-color-by-theme color 50 60)]
    [linear-gradient/linear-gradient {:colors [bg-color "rgba(0,0,0,0)"]
                                      :start {:x 0 :y 0} :end {:x 0 :y 1}}
     [rn/view {:style {:padding-left     60
                       :padding-vertical 12
                       :padding-right    24}}
      [text/text
       {:size   :paragraph-2
        :weight :medium
        :style  {:color text-color}}
       label]]]))
