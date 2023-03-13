(ns status-im2.contexts.chat.messages.content.audio.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]))

(defn container
  []
  {:width            295
   :height           56
   :border-radius    12
   :border-width     1
   :padding          12
   :flex-direction   :row
   :align-items      :center
   :justify-content  :space-between
   :border-color     (colors/theme-colors colors/neutral-20 colors/neutral-80)
   :background-color (colors/theme-colors colors/neutral-5 colors/neutral-80-opa-40)})

(def play-pause-slider-container
  {:flex-direction :row
   :align-items    :center})

(def slider-container
  {:position :absolute
   :left     60
   :right    71
   :bottom   nil})

(defn play-pause-container
  []
  {:background-color (get-in colors/customization [:blue (if (theme/dark?) 60 50)])
   :width            32
   :height           32
   :border-radius    16
   :align-items      :center
   :justify-content  :center})

(def timestamp
  {:margin-left 4})

(def error-label
  {:margin-bottom 16})
