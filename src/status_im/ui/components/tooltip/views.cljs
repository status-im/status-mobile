(ns status-im.ui.components.tooltip.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.animation :as animation]
            [status-im.ui.components.tooltip.animations :as animations]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.tooltip.styles :as styles]))

(views/defview tooltip [label & [{:keys [bottom-value color font-size] :or {bottom-value -30 color :white font-size 15}}]]
  (views/letsubs [bottom-anim-value (animation/create-value bottom-value)
                  opacity-value     (animation/create-value 0)]
    {:component-did-mount (animations/animate-tooltip bottom-value bottom-anim-value opacity-value)}
    [react/view styles/tooltip-container
     [react/animated-view {:style (styles/tooltip-animated bottom-value opacity-value)}
      [react/view (styles/tooltip-text-container color)
       [react/text {:style (styles/tooltip-text font-size)} label]]
      [vector-icons/icon :icons/tooltip-triangle {:color color :style styles/tooltip-triangle}]]]))
