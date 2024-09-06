(ns status-im.contexts.chat.messenger.messages.content.audio.style
  (:require
    [quo.foundations.colors :as colors]
    [quo.theme]))

(defn container
  [theme]
  {:width            295
   :height           56
   :border-radius    12
   :border-width     1
   :padding          12
   :flex-direction   :row
   :align-items      :center
   :justify-content  :space-between
   :border-color     (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
   :background-color (colors/theme-colors colors/neutral-5 colors/neutral-80-opa-40 theme)})

(def slider-container
  {:position :absolute
   :left     60
   :right    71
   :bottom   nil})

(defn play-pause-container
  [theme]
  {:background-color (get-in colors/customization [:blue (if (= :dark theme) 60 50)])
   :width            32
   :height           32
   :border-radius    16
   :align-items      :center
   :justify-content  :center})

(def timestamp
  {:margin-left 4})

(def error-label
  {:margin-bottom 16})
