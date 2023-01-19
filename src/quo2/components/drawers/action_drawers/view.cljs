(ns quo2.components.drawers.action-drawers.view
  (:require [quo2.components.icon :as icon]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [quo2.components.drawers.action-drawers.style :as style]))

(defn- get-icon-color
  [danger?]
  (if danger?
    colors/danger-50
    (colors/theme-colors colors/neutral-50 colors/neutral-40)))

(def divider
  [rn/view
   {:style               style/divider
    :accessible          true
    :accessibility-label :divider}])

(defn action
  [{:keys [icon
           label
           sub-label
           right-icon
           danger?
           on-press
           add-divider?
           accessibility-label]
    :as   action-props}]
  (when action-props
    [:<> {:key label}
     (when add-divider? divider)
     [rn/touchable-highlight
      {:accessibility-label accessibility-label
       :style               style/container
       :underlay-color      (colors/theme-colors colors/neutral-5 colors/neutral-90)
       :on-press            on-press}
      [rn/view
       {:style style/row-container}
       [rn/view
        {:accessibility-label :left-icon-for-action
         :accessible          true
         :style               style/left-icon}
        [icon/icon icon
         {:color (get-icon-color danger?)
          :size  20}]]
       [rn/view
        {:style style/text-container}
        [text/text
         {:size   :paragraph-1
          :weight :medium
          :style  {:color
                   (when danger?
                     (colors/theme-colors colors/danger-50 colors/danger-60))}}
         label]
        (when sub-label
          [text/text
           {:size  :paragraph-2
            :style {:color
                    (colors/theme-colors colors/neutral-50 colors/neutral-40)}}
           sub-label])]
       (when right-icon
         [rn/view
          {:style               style/right-icon
           :accessible          true
           :accessibility-label :right-icon-for-action}
          [icon/icon right-icon
           {:color (get-icon-color danger?)
            :size  20}]])]]]))

(defn action-drawer
  [sections]
  [:<>
   (doall
    (for [actions sections]
      (doall
       (map action actions))))])
