(ns status-im2.contexts.shell.components.home-stack.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.shell.utils :as utils]))

(defn home-stack
  [shared-values {:keys [width height]}]
  (reanimated/apply-animations-to-style
   {:top            (:home-stack-top shared-values)
    :left           (:home-stack-left shared-values)
    :opacity        (:home-stack-opacity shared-values)
    :pointer-events (:home-stack-pointer shared-values)
    :transform      [{:scale (:home-stack-scale shared-values)}]}
   {:border-bottom-left-radius  20
    :border-bottom-right-radius 20
    :background-color           (colors/theme-colors colors/white colors/neutral-95)
    :overflow                   :hidden
    :position                   :absolute
    :width                      width
    :height                     (- height (utils/bottom-tabs-container-height))}))

(defn stack-view
  [stack-id {:keys [opacity z-index]}]
  (reanimated/apply-animations-to-style
   {:opacity opacity
    :z-index z-index}
   {:position            :absolute
    :top                 0
    :bottom              0
    :left                0
    :right               0
    :accessibility-label stack-id}))
