(ns quo2.components.drawers.action-drawers.view
  (:require [quo2.components.icon :as icon]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [quo2.components.drawers.action-drawers.style :as style]))

(defn- get-icon-color
  [danger? override-theme]
  (if danger?
    colors/danger-50
    (colors/theme-colors colors/neutral-50 colors/neutral-40 override-theme)))

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
           override-theme
           accessibility-label]
    :as   action-props}]
  (when action-props
    [:<> {:key label}
     (when add-divider? divider)
     [rn/touchable-highlight
      {:accessibility-label accessibility-label
       :style               (style/container sub-label)
       :underlay-color      (colors/theme-colors colors/neutral-5 colors/neutral-90 override-theme)
       :on-press            on-press}
      [rn/view
       {:style (style/row-container sub-label)}
       [rn/view
        {:accessibility-label :left-icon-for-action
         :accessible          true
         :style               style/left-icon}
        [icon/icon icon
         {:color (get-icon-color danger? override-theme)
          :size  20}]]
       [rn/view
        {:style style/text-container}
        [text/text
         {:size   :paragraph-1
          :weight :medium
          :style  {:color
                   (cond
                     danger? (colors/theme-colors colors/danger-50 colors/danger-60 override-theme)
                     :else   (colors/theme-colors colors/neutral-100 colors/white override-theme))}}
         label]
        (when sub-label
          [text/text
           {:size  :paragraph-2
            :style {:color
                    (colors/theme-colors colors/neutral-50 colors/neutral-40 override-theme)}}
           sub-label])]
       (when right-icon
         [rn/view
          {:style               style/right-icon
           :accessible          true
           :accessibility-label :right-icon-for-action}
          [icon/icon right-icon
           {:color (get-icon-color danger? override-theme)
            :size  20}]])]]]))

(defn action-drawer
  [sections]
  [:<>
   (doall
    (for [actions sections]
      (doall
       (map action actions))))])
