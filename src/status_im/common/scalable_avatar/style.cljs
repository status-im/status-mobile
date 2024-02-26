(ns status-im.common.scalable-avatar.style
  (:require [quo.foundations.colors :as colors]))

(defn wrapper
  [theme scale-animation top-margin-animation side-margin-animation border-color]
  [{:transform     [{:scale scale-animation}]
    :margin-top    top-margin-animation
    :margin-left   side-margin-animation
    :margin-bottom side-margin-animation}
   {:border-width  4
    :border-color  (or border-color
                       (colors/theme-colors colors/border-avatar-light colors/neutral-80-opa-80 theme))
    :border-radius 100}])
