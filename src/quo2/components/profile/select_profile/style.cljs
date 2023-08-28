(ns quo2.components.profile.select-profile.style
  (:require [quo2.foundations.colors :as colors]))

(defn container
  [customization-color selected?]
  {:padding          12
   :background-color (colors/custom-color customization-color 50 40)
   :border-width     1
   :border-radius    16
   :flex             1
   :border-color     (if selected? colors/white-opa-40 :transparent)})

(def header
  {:flex-direction  :row
   :justify-content :space-between})

(def profile-name
  {:margin-top 8
   :color      colors/white})

(defn select-radio
  [selected?]
  {:background-color (if selected? :transparent colors/white-opa-5)
   :border-width     1.2
   :width            20
   :height           20
   :border-radius    10
   :justify-content  :center
   :align-items      :center
   :border-color     (if selected? colors/white colors/white-opa-40)})

(def select-radio-inner
  {:background-color colors/white
   :border-radius    7
   :width            14
   :height           14})

