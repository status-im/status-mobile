(ns status-im.ui.screens.onboarding.intro.styles
  (:require [quo.animated :as animated]
            [quo.design-system.colors :as colors]))

(def dot-size 6)
(def progress-size 36)

(def intro-view
  {:flex            1
   :justify-content :flex-end
   :margin-bottom   12})

(defn dot-selector
  []
  {:flex-direction  :row
   :justify-content :space-between
   :margin-bottom   16
   :align-items     :center})

(defn dot-style
  [active]
  {:background-color  colors/blue-light
   :overflow          :hidden
   :opacity           1
   :margin-horizontal 2
   :width             (animated/mix active dot-size progress-size)
   :height            dot-size
   :border-radius     3})

(defn dot-progress
  [active progress]
  {:background-color colors/blue
   :height           dot-size
   :width            progress-size
   :opacity          (animated/mix active 0 1)
   :transform        [{:translateX (animated/mix progress (- progress-size) 0)}]})

(defn wizard-text-with-height
  [height]
  (merge {:color      colors/gray
          :text-align :center}
         (when-not (zero? height)
           {:height height})))

(def wizard-title
  {:margin-bottom 16
   :text-align    :center})
