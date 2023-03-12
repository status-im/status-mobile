(ns status-im2.common.mute-chat-drawer.style
  (:require [quo2.foundations.colors :as colors]))

(defn header-text
  []
  {:margin-left   20
   :margin-bottom 10
   :color         (colors/theme-colors colors/neutral-50 colors/neutral-40)})