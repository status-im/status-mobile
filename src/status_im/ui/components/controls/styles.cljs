(ns status-im.ui.components.controls.styles
  (:require [status-im.ui.components.colors :as colors]))

(defn switch-style
  [state disabled]
  {:width            52
   :height           28
   :border-radius    14
   :padding          4
   :background-color (if state
                       (:ui-01 @colors/theme)
                       (if disabled
                         (:interactive-04 @colors/theme)
                         (:interactive-01 @colors/theme)))})

(defn switch-bullet-style
  [state hold]
  {:width            20
   :height           20
   :border-radius    10
   :opacity          (if hold 1 0.6)
   :transform        [{:translateX (if state 0 24)}]
   :background-color colors/white-persist
   :elevation        4
   :shadow-opacity   1
   :shadow-radius    16
   :shadow-color     (:shadow-01 @colors/theme)
   :shadow-offset    {:width 0 :height 4}})

(defn radio-style
  [state disabled]
  {:width            20
   :height           20
   :border-radius    10
   :padding          4
   :background-color (if state
                       (:ui-01 @colors/theme)
                       (if disabled
                         (:interactive-04 @colors/theme)
                         (:interactive-01 @colors/theme)))})

(defn radio-bullet-style
  [state hold]
  {:width            12
   :height           12
   :border-radius    6
   :opacity          (if hold 1 0.6)
   :transform        [{:scale (if state 0.0001 1)}]
   :background-color colors/white-persist
   :elevation        4
   :shadow-opacity   1
   :shadow-radius    16
   :shadow-color     (:shadow-01 @colors/theme)
   :shadow-offset    {:width 0 :height 4}})

(defn animated-checkbox-style
  [state disabled]
  {:width            18
   :height           18
   :border-radius    4
   :justify-content  :center
   :align-items      :center
   :background-color (if state
                       (:ui-01 @colors/theme)
                       (if disabled
                         (:interactive-04 @colors/theme)
                         (:interactive-01 @colors/theme)))})

(defn checkbox-style
  [value disabled]
  {:width            18
   :height           18
   :border-radius    4
   :justify-content  :center
   :align-items      :center
   :background-color (if value
                       (if disabled
                         (:interactive-04 @colors/theme)
                         (:interactive-01 @colors/theme))
                       (:ui-01 @colors/theme))})

(defn animated-check-icon-style
  [state hold]
  {:opacity   (if hold 1 0.6)
   :transform [{:scale (if state 0.0001 1)}]})

(defn check-icon-style
  [value]
  {:opacity (if value 1 0)})
