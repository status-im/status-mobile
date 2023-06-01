(ns quo2.components.reactions.reaction
  (:require [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.components.reactions.resource :as resource]
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
     [icons/icon :i/add-reaction
      {:size  20
       :color (if dark?
                colors/white
                colors/neutral-100)}]]))

(defn reaction
  "Add your emoji as a param here"
  [{:keys [emoji clicks neutral? on-press accessibility-label on-long-press]}]
  (let [numeric-value (int clicks)]
    [rn/touchable-opacity
     {:on-press            on-press
      :on-long-press       on-long-press
      :accessibility-label accessibility-label
      :style               (style/reaction neutral?)}
     [rn/image
      {:style               {:width 16 :height 16}
       :accessibility-label :emoji
       :source              (resource/get-reaction emoji)}]
     [text/text
      {:size            :paragraph-2
       :weight          :semi-bold
       :flex-direction  :row
       :align-items     :center
       :justify-content :center}
      (when (pos? numeric-value)
        (str " " numeric-value))]]))
