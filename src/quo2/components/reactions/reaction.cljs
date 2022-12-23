(ns quo2.components.reactions.reaction
  (:require [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.components.reactions.style :as style]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.core :as rn]))

(defn add-reaction
  [{:keys [on-press]}]
  (let [dark? (theme/dark?)]
    [rn/touchable-opacity
     {:on-press            on-press
      :accessibility-label :emoji-reaction-add
      :style               (style/add-reaction)}
     [icons/icon :i/add
      {:size  20
       :color (if dark?
                colors/white
                colors/neutral-100)}]]))

(defn reaction
  "Add your emoji as a param here"
  [{:keys [emoji clicks neutral? on-press accessibility-label]}]
  (let [numeric-value (int clicks)]
    [rn/touchable-opacity
     {:on-press            on-press
      :accessibility-label accessibility-label
      :style               (style/reaction neutral?)}
     [icons/icon emoji {:no-color true
                        :size     16}]
     [text/text {:size            :paragraph-2
                 :weight          :semi-bold
                 :flex-direction  :row
                 :align-items     :center
                 :justify-content :center}
      (when (pos? numeric-value)
        (str " " numeric-value))]]))
