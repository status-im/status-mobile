(ns status-im.ui.screens.intro.styles
  (:require [status-im.ui.components.colors :as colors]
            [quo.animated :as animated]))

(def dot-size 6)
(def progress-size 36)

(def intro-view
  {:flex               1
   :justify-content    :flex-end
   :margin-bottom      12})

(defn dot-selector []
  {:flex-direction  :row
   :justify-content :space-between
   :align-items     :center})

(defn dot-style [active]
  {:background-color  colors/blue-light
   :overflow          :hidden
   :opacity           1
   :margin-horizontal 2
   :width             (animated/mix active dot-size progress-size)
   :height            dot-size
   :border-radius     3})

(defn dot-progress [active progress]
  {:background-color colors/blue
   :height           dot-size
   :width            progress-size
   :opacity          (animated/mix active 0 1)
   :transform        [{:translateX (animated/mix progress (- progress-size) 0)}]})

(def wizard-title
  {:margin-bottom 16
   :typography    :header
   :text-align    :center})

(def wizard-text
  {:color      colors/gray
   :text-align :center})

(defn wizard-text-with-height [height]
  (merge wizard-text
         (when-not (zero? height)
           {:height height})))

(def welcome-text-bottom-note
  {:typography  :caption
   :color       colors/gray
   :text-align  :center})

(defn list-item [selected?]
  {:flex-direction   :row
   :align-items      :center
   :justify-content  :space-between
   :padding-left     16
   :padding-right    10
   :background-color (if selected? colors/blue-light colors/white)
   :padding-vertical 12})

(def multiaccount-image
  {:width            40
   :height           40
   :border-radius    20
   :border-width     1
   :border-color     colors/black-transparent})

(defn password-text-input [width]
  {:typography :header
   :width      width})

(def buttons-container
  {:align-items        :center
   :padding-horizontal 32})

(def bottom-button
  {:padding-horizontal 24
   :justify-content    :center
   :align-items        :center
   :flex-direction     :row})