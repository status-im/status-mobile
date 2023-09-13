(ns quo2.components.calendar.calendar.years-list.style
  (:require [quo2.foundations.colors :as colors]))

(defn gradient-start-color
  [theme]
  (colors/theme-colors colors/white colors/neutral-90 theme))

(defn gradient-end-color
  [theme]
  (colors/theme-colors colors/white-opa-0 colors/neutral-100-opa-0 theme))

(def gradient-view
  {:position               :absolute
   :height                 50
   :border-top-left-radius 12
   :top                    0
   :left                   0
   :right                  0})

(defn container-years
  [theme]
  {:border-width              1
   :overflow                  :hidden
   :padding-left              8
   :padding-right             7
   :padding-vertical          8
   :margin-left               -1
   :margin-top                -1
   :margin-bottom             -1
   :border-style              :dashed
   :border-top-left-radius    12
   :border-bottom-left-radius 12
   :border-color              (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)})
