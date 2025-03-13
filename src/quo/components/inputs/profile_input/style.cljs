(ns quo.components.inputs.profile-input.style
  (:require
    [quo.foundations.colors :as colors]))

(defn container
  [customization-color]
  {:background-color   (colors/resolve-color customization-color :light 40)
   :padding-horizontal 12
   :padding-top        12
   :padding-bottom     10
   :border-radius      16
   :flex               1})

(def button
  {:position      :absolute
   :top           24
   :bottom        0
   :left          33
   :right         0
   :width         24
   :height        24
   :border-radius 24})

(def button-inner {:border-radius 24})

(def input-container
  {:flex-direction :row
   :margin-top     4
   :align-items    :center})
