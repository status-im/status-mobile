(ns quo2.components.keycard.style
  (:require [quo2.foundations.colors :as colors]))

(def keycard-height 210)
(def keycard-chip-height 28)

(defn card-container
  [locked?]
  {:overflow         :hidden
   :height           keycard-height
   :align-items      :flex-start
   :justify-content  :space-between
   :border-width     1
   :border-color     (if locked?
                       colors/white-opa-5
                       (colors/theme-colors colors/neutral-20 colors/neutral-80))
   :border-style     :solid
   :border-radius    16
   :padding          16
   :background-color (if locked?
                       (colors/theme-colors colors/danger colors/danger-opa-40)
                       (colors/theme-colors colors/neutral-5 colors/neutral-90))})

(defn keycard-logo
  [locked?]
  {:tint-color (if locked? colors/white (colors/theme-colors colors/neutral-100 colors/white))})

(defn keycard-watermark
  [locked?]
  {:position   :absolute
   :tint-color (if locked? colors/white-opa-5 (colors/theme-colors colors/neutral-100 colors/white))
   :align-self :center
   :height     280.48
   :transform  [{:rotate "-30deg"} {:translateY -30}]
   :opacity    (when (not (boolean locked?)) 0.02)
   :z-index    1})

(def keycard-chip
  {:height   keycard-chip-height
   :position :absolute
   :right    16
   :top      (/ (- keycard-height 28) 2)})
