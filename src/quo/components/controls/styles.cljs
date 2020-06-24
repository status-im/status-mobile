(ns quo.components.controls.styles
  (:require [quo.animated :as animated]
            [quo.design-system.colors :as colors]))

(defn switch-style [state]
  {:width            52
   :height           28
   :border-radius    14
   :padding          4
   :background-color (animated/mix-color state
                                         (:ui-01 @colors/theme)
                                         (:interactive-01 @colors/theme))})

(defn switch-bullet-style [state hold]
  {:width            20
   :height           20
   :border-radius    10
   :opacity          (animated/mix hold 1 0.6)
   :transform        [{:translateX (animated/mix state 0 24)}]
   :background-color colors/white
   :elevation        4
   :shadow-opacity   1
   :shadow-radius    16
   :shadow-color     (:shadow-01 @colors/theme)
   :shadow-offset    {:width 0 :height 4}})

(defn radio-style [state]
  {:width            20
   :height           20
   :border-radius    10
   :padding          4
   :background-color (animated/mix-color state
                                         (:ui-01 @colors/theme)
                                         (:interactive-01 @colors/theme))})

(defn radio-bullet-style [state hold]
  {:width            12
   :height           12
   :border-radius    6
   :opacity          (animated/mix hold 1 0.6)
   :transform        [{:scale (animated/mix state 0.0001 1)}]
   :background-color colors/white
   :elevation        4
   :shadow-opacity   1
   :shadow-radius    16
   :shadow-color     (:shadow-01 @colors/theme)
   :shadow-offset    {:width 0 :height 4}})

(defn checkbox-style [state]
  {:width            18
   :height           18
   :border-radius    4
   :justify-content  :center
   :align-items      :center
   :background-color (animated/mix-color state
                                         (:ui-01 @colors/theme)
                                         (:interactive-01 @colors/theme))})

(defn check-icon-style [state hold]
  {:opacity       (animated/mix hold 1 0.6)
   :transform     [{:scale (animated/mix state 0.0001 1)}]})
