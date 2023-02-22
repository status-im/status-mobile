(ns quo2.components.input.style
  (:require [quo2.foundations.colors :as colors]))

(def status-colors
  {:dark-blur {:default {:border-color      colors/white-opa-10
                         :placeholder-color colors/white-opa-40
                         :cursor-color      colors/white}
               :focus   {:border-color      colors/white-opa-40
                         :placeholder-color colors/white-opa-20
                         :cursor-color      colors/white}
               :error   {:border-color      colors/danger-opa-40
                         :placeholder-color colors/white-opa-40
                         :cursor-color      colors/white}}})

(defn input
  [colors-by-status]
  {:height           40
   :border-width     1
   :border-color     (:border-color colors-by-status)
   :padding-vertical 9
   :padding-left     16
   :padding-right    40
   :border-radius    14
   :color            colors/white})

(def right-icon-touchable-area
  {:position         :absolute
   :right            0
   :top              0
   :bottom           0
   :width            32
   :padding-vertical 9
   :padding-right    12
   :justify-content  :center
   :align-items      :center})

(def icon
  {:color colors/white
   :size  20})
