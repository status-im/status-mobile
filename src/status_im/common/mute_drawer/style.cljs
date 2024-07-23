(ns status-im.common.mute-drawer.style
  (:require
    [quo.foundations.colors :as colors]))

(defn header-text
  [theme]
  {:margin-left   20
   :margin-bottom 10
   :color         (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})
