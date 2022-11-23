(ns status-im2.contexts.communities.overview.style
  (:require [quo2.foundations.colors :as colors]))

(defn container1 []
  {:flex             1
   :height           20
   :border-radius    16
   :background-color (colors/theme-colors colors/white colors/neutral-90)})

(defn container2 []
  {:border-radius    40
   :border-width     1
   :border-color     colors/white
   :position         :absolute
   :top              (- (/ 80 2))
   :left             (/ 70 4)
   :padding          2
   :background-color (colors/theme-colors colors/white colors/neutral-90)})

(def preview-user
  {:flex-direction :row
   :align-items    :center
   :margin-top     20})
