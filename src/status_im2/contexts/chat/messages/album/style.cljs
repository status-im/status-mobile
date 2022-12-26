(ns status-im2.contexts.chat.messages.album.style
  (:require [quo2.foundations.colors :as colors]))

(def overlay
  {:position         :absolute
   :width            73
   :height           73
   :background-color colors/neutral-80-opa-60
   :justify-content  :center
   :align-items      :center})
