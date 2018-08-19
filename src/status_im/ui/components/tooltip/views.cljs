(ns status-im.ui.components.tooltip.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.tooltip.animations :as animations]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.tooltip.styles :as styles]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as components.styles]
            [reagent.core :as reagent]
            [status-im.utils.utils :as utils]))

(views/defview tooltip [label & [{:keys [bottom-value color font-size font-color delay]
                                  :or {bottom-value -30
                                       color :white
                                       font-size 15
                                       font-color components.styles/color-red-2}}]]
  (views/letsubs [bottom-anim-value (animation/create-value bottom-value)
                  opacity-value     (animation/create-value 0)]
    {:component-did-mount (animations/animate-tooltip bottom-value
                                                      bottom-anim-value
                                                      opacity-value
                                                      delay
                                                      10)}
    [react/view styles/tooltip-container
     [react/animated-view {:style (styles/tooltip-animated bottom-anim-value opacity-value)}
      [react/view (styles/tooltip-text-container color)
       [react/text {:style (styles/tooltip-text font-size font-color)} label]]
      [vector-icons/icon :icons/tooltip-triangle {:color color :style styles/tooltip-triangle}]]]))

(views/defview bottom-tooltip-info [label on-close]
  (views/letsubs [bottom-anim-value (animation/create-value -150)
                  opacity-value     (animation/create-value 0)]
    {:component-did-mount (animations/animate-tooltip -150 bottom-anim-value opacity-value nil -10)}
    [react/view styles/bottom-tooltip-container
     [react/animated-view {:style (styles/tooltip-animated bottom-anim-value opacity-value)}
      [vector-icons/icon :icons/tooltip-triangle {:color           colors/gray-notifications
                                                  :style           styles/tooltip-triangle
                                                  :container-style {:transform [{:rotate "180deg"}]}}]
      [react/view styles/bottom-tooltip-text-container
       [react/text {:style styles/bottom-tooltip-text} label]
       [react/touchable-highlight {:on-press on-close
                                   :style    styles/close-icon}
        [vector-icons/icon :icons/close {:color colors/white}]]]]]))
