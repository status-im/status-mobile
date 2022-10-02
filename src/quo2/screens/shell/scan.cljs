(ns quo2.screens.shell.scan
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.shell.scan :as scan]))

(defn preview-scan []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}
   [scan/preview]])
