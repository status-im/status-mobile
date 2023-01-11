(ns status-im2.contexts.chat.messages.style
  (:require [quo2.foundations.colors :as colors]))

(defn banners
  []
  {:position         :absolute
   :top              56
   :z-index          2
   :background-color (colors/theme-colors colors/white colors/neutral-100)})
