(ns status-im.ui.components.tooltip.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.animation :as animation]
            [status-im.ui.components.tooltip.animations :as animations]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.tooltip.styles :as styles]
            [status-im.ui.components.colors :as colors]
            [reagent.core :as reagent]))

(views/defview tooltip [label & [{:keys [bottom-value color font-size container-style]
                                  :or {bottom-value 30 color colors/white font-size 15}}]]
  (views/letsubs [bottom-anim-value (animation/create-value bottom-value)
                  opacity-value     (animation/create-value 0)]
    {:component-did-mount (animations/animate-tooltip bottom-value bottom-anim-value opacity-value -10)}
    [react/view (merge styles/tooltip-container container-style)
     [react/animated-view {:style (styles/tooltip-animated bottom-anim-value opacity-value)}
      (when label
        [react/view (styles/tooltip-text-container color)
         [react/text {:style (styles/tooltip-text font-size)} label]])
      #_[vector-icons/icon :icons/tooltip-triangle (assoc
                                                    styles/tooltip-triangle
                                                    :color color)]]]))

(views/defview bottom-tooltip-info [label on-close]
  (views/letsubs [bottom-anim-value (animation/create-value 150)
                  opacity-value     (animation/create-value 0)]
    {:component-did-mount (animations/animate-tooltip 150 bottom-anim-value opacity-value 10)}
    [react/view styles/bottom-tooltip-container
     [react/animated-view {:style (styles/tooltip-animated bottom-anim-value opacity-value)}
      [vector-icons/icon :icons/tooltip-triangle (assoc
                                                  styles/tooltip-triangle
                                                  :color colors/gray
                                                  :container-style {:transform [{:rotate "180deg"}]})]
      [react/view styles/bottom-tooltip-text-container
       [react/text {:style styles/bottom-tooltip-text} label]
       [react/touchable-highlight {:on-press on-close
                                   :style    styles/close-icon}
        [vector-icons/icon :main-icons/close {:color colors/white}]]]]]))
