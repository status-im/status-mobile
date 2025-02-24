(ns quo.components.keycard.style
  (:require
    [quo.foundations.colors :as colors]))

(def keycard-height 210)
(def keycard-chip-height 28)

(defn card-container
  [locked? theme blur?]
  {:overflow         :hidden
   :height           keycard-height
   :align-items      :flex-start
   :justify-content  :space-between
   :border-width     1
   :border-color     (if (or locked? blur?)
                       colors/white-opa-5
                       (colors/theme-colors colors/neutral-20 colors/neutral-80 theme))
   :border-style     :solid
   :border-radius    16
   :padding          16
   :background-color (if locked?
                       (colors/theme-colors colors/danger-50 colors/danger-50-opa-40 theme)
                       colors/keycard-color)})

(defn keycard-logo
  [locked? theme]
  {:tint-color (if locked?
                 colors/white
                 (colors/theme-colors colors/neutral-100 colors/white theme))})

(def keycard-chip
  {:height   keycard-chip-height
   :position :absolute
   :right    16
   :top      (/ (- keycard-height 28) 2)})
