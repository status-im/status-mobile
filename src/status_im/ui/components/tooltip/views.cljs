(ns status-im.ui.components.tooltip.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.animation :as animation]
            [status-im.ui.components.tooltip.animations :as animations]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.tooltip.styles :as styles]
            [status-im.ui.components.colors :as colors]
            [reagent.core :as reagent]))

(views/defview tooltip [label & [{:keys [bottom-value color font-size] :or {bottom-value -30 color :white font-size 15}}]]
  (views/letsubs [bottom-anim-value (animation/create-value bottom-value)
                  opacity-value     (animation/create-value 0)]
    {:component-did-mount (animations/animate-tooltip bottom-value bottom-anim-value opacity-value 10)}
    [react/view styles/tooltip-container
     [react/animated-view {:style (styles/tooltip-animated bottom-anim-value opacity-value)}
      [react/view (styles/tooltip-text-container color)
       [react/text {:style (styles/tooltip-text font-size)} label]]
      [vector-icons/icon :icons/tooltip-triangle {:color color :style styles/tooltip-triangle}]]]))

(views/defview bottom-tooltip-info [label]
  (views/letsubs [opened?           (reagent/atom true)
                  bottom-anim-value (animation/create-value -150)
                  opacity-value     (animation/create-value 0)]
    {:component-did-mount (animations/animate-tooltip -150 bottom-anim-value opacity-value -10)}
    (when @opened?
      [react/view styles/bottom-tooltip-container
       [react/animated-view {:style (styles/tooltip-animated bottom-anim-value opacity-value)}
        [vector-icons/icon :icons/tooltip-triangle {:color           colors/gray-notifications
                                                    :style           styles/tooltip-triangle
                                                    :container-style {:transform [{:rotate "180deg"}]}}]
        [react/view styles/bottom-tooltip-text-container
         [react/text {:style styles/bottom-tooltip-text} label]
         [react/touchable-highlight {:on-press #(reset! opened? false)
                                     :style    styles/close-icon}
          [vector-icons/icon :icons/close {:color colors/white}]]]]])))
