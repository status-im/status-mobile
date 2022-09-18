(ns quo2.components.dividers.new-messages
  (:require [quo.react-native :as rn]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]))

(defn new-messages
  "new-messages params - label, color"
  [{:keys [label color] :or {label (i18n/label :new-messages-header)
                             color :primary}}]
  (let [bg-color   (colors/custom-color-by-theme color 50 60 5 5)
        text-color (colors/custom-color-by-theme color 50 60)]
    [react/linear-gradient {:colors [bg-color "rgba(0,0,0,0)"]
                            :start {:x 0 :y 0} :end {:x 0 :y 1}}
     [rn/view {:style {:padding-left     60
                       :padding-vertical 12
                       :padding-right    24}}
      [text/text
       {:size   :paragraph-2
        :weight :medium
        :style  {:color text-color}}
       label]]]))
