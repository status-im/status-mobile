(ns quo.components.text-combinations.style
  (:require [quo.foundations.colors :as colors]))

(def title-container
  {:flex-direction :row
   :flex           1
   :align-items    :center})

(def avatar-container
  {:margin-right 9
   :text-align   :center})

(def description-description-text
  {:margin-top 8})

(def emoji-hash
  {:margin-top     8
   :letter-spacing 2
   :font-size      13
   :line-height    20.5})

(defn textual-emoji
  [size customization-color theme]
  {:border-radius    size
   :margin-top       -5
   :border-width     0
   :border-color     :transparent
   :width            size
   :height           size
   :justify-content  :center
   :align-items      :center
   :background-color (colors/resolve-color customization-color theme 10)})
