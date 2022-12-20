(ns status-im.ui.components.tooltip.views
  (:require [quo.design-system.colors :as colors]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.tooltip.animations :as animations]
            [status-im.ui.components.tooltip.styles :as styles])
  (:require-macros [status-im.utils.views :as views]))

(views/defview tooltip
  [label &
   [{:keys [bottom-value color font-size container-style]
     :or   {bottom-value 30 color colors/white font-size 15}}
    accessibility-label]]
  (views/letsubs [bottom-anim-value (animation/create-value bottom-value)
                  opacity-value     (animation/create-value 0)]
    {:component-did-mount (animations/animate-tooltip bottom-value bottom-anim-value opacity-value -10)}
    [react/view (merge styles/tooltip-container container-style)
     [react/animated-view {:style (styles/tooltip-animated bottom-anim-value opacity-value)}
      (when label
        [react/view (styles/tooltip-text-container color)
         [react/text
          {:style               (styles/tooltip-text font-size)
           :accessibility-label accessibility-label}
          label]])
      #_[icons/icon :icons/tooltip-tip
         (assoc
          styles/tooltip-triangle
          :color
          color)]]]))

(views/defview bottom-tooltip-info
  [label on-close]
  (views/letsubs [bottom-anim-value (animation/create-value 75)
                  opacity-value     (animation/create-value 0)]
    {:component-did-mount (animations/animate-tooltip 75 bottom-anim-value opacity-value 10)}
    [react/view styles/bottom-tooltip-container
     [react/animated-view {:style (styles/tooltip-animated bottom-anim-value opacity-value)}
      [icons/icon :icons/tooltip-tip
       (assoc
        styles/tooltip-triangle
        :color           colors/gray
        :container-style {:transform [{:rotate "180deg"}]})]
      [react/view styles/bottom-tooltip-text-container
       [react/text {:style styles/bottom-tooltip-text} label]
       [react/touchable-highlight
        {:on-press on-close
         :style    styles/close-icon}
        [icons/icon :main-icons/close {:color colors/white}]]]]]))
