(ns quo.components.community.channel-actions
  (:require
    [quo.components.community.style :as style]
    [quo.components.counter.counter.view :as counter]
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [react-native.core :as rn]))

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
       [counter/view {:type :secondary} counter-value])]
    [text/text {:size :paragraph-1 :weight :medium :number-of-lines 2} label]]])

(defn channel-actions
  [{:keys [container-style actions]}]
  [rn/view {:style (assoc container-style :flex-direction :row)}
   (map-indexed
    (fn [index action]
      ^{:key index}
      [:<>
       [channel-action action]
       (when (not= action (last actions))
         [rn/view {:width 16}])])
    actions)])
