(ns status-im2.contexts.chat.messages.composer.style
  (:require [quo2.foundations.colors :as colors]
            [status-im.utils.platform :as platform]))

(defn input-bottom-sheet
  [window-height]
  (merge {:border-top-left-radius  20
          :border-top-right-radius 20
          :position                :absolute
          :left                    0
          :right                   0
          :bottom                  (- window-height)
          :height                  window-height
          :flex                    1
          :background-color        (colors/theme-colors colors/white colors/neutral-90)
          :z-index                 2}
         (if platform/ios?
           {:shadow-radius  16
            :shadow-opacity 1
            :shadow-color   "rgba(9, 16, 28, 0.04)"
            :shadow-offset  {:width 0 :height -2}}
           {:elevation 4})))

(defn bottom-sheet-handle
  []
  {:width            32
   :height           4
   :background-color (colors/theme-colors colors/neutral-100 colors/white)
   :opacity          0.05
   :border-radius    100
   :align-self       :center
   :margin-top       8})

(defn bottom-sheet-background
  [window-height]
  {:position         :absolute
   :left             0
   :right            0
   :bottom           0
   :height           window-height
   :background-color colors/neutral-95-opa-70
   :z-index          1})
