(ns quo2.components.community.channel-actions
  (:require [react-native.core :as rn]
            [quo2.components.icon :as icons]
            [quo2.components.counter.counter :as counter]
            [quo2.components.markdown.text :as text]
            [quo2.components.community.style :as style]))

(defn channel-action
  [{:keys [big? color label counter-value icon on-press accessibility-label]}]
  [rn/touchable-opacity
   {:on-press            on-press
    :style               (style/channel-action-touch big?)
    :accessibility-label accessibility-label}
   [rn/view {:style (style/channel-action color)}
    [rn/view {:style style/channel-action-row}
     [icons/icon icon]
     (when counter-value
       [counter/counter {:type :secondary} counter-value])]
    [text/text {:size :paragraph-1 :weight :medium} label]]])

(defn channel-actions
  [{:keys [style actions]}]
  [rn/view {:style (merge {:flex-direction :row :flex 1} style)}
   (for [action actions]
     [:<>
      [channel-action action]
      (when (not= action (last actions))
        [rn/view {:width 16}])])])
