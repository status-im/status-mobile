(ns status-im.contexts.shell.home-stack.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.reanimated :as reanimated]))

(defn stack-view
  [stack-id {:keys [opacity z-index]}]
  (reanimated/apply-animations-to-style
   {:opacity opacity
    :z-index z-index}
   {:top                 0
    :left                0
    :right               0
    :bottom              0
    :position            :absolute
    :accessibility-label stack-id}))

(defn home-stack
  [theme]
  {:border-bottom-left-radius  20
   :border-bottom-right-radius 20
   :background-color           (colors/theme-colors colors/white colors/neutral-95 theme)
   :flex                       1})
