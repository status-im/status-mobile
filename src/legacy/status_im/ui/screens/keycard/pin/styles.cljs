(ns legacy.status-im.ui.screens.keycard.pin.styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]))

(def pin-container
  {:flex            1
   :flex-direction  :column
   :justify-content :space-between})

(defn info-container
  [small-screen?]
  {:height          44
   :width           "100%"
   :justify-content :center
   :margin-top      (if small-screen? 14 10)})

(defn error-container
  [y-translation opacity]
  {:left            0
   :right           0
   :align-items     :center
   :position        :absolute
   :transform       [{:translateY y-translation}]
   :opacity         opacity
   :justify-content :center})

(defn error-text
  [small-screen?]
  {:position   :absolute
   :color      colors/red
   :font-size  (if small-screen? 12 15)
   :text-align :center})

(defn retry-container
  [y-translation opacity]
  {:left            0
   :right           0
   :align-items     :center
   :position        :absolute
   :transform       [{:translateY y-translation}]
   :opacity         opacity
   :justify-content :center})

(defn center-container
  [title]
  {:flex-direction :column
   :align-items    :center
   :margin-top     (if title 20 5)})

(def center-title-text
  {:typography :header})

(def create-pin-text
  {:padding-top 8
   :width       314
   :text-align  :center
   :color       colors/gray})

(def pin-indicator-container
  {:flex-direction  :row
   :justify-content :space-between
   :align-items     :center
   :height          22
   :margin-top      5})

(defn pin-indicator
  [pressed? error?]
  {:width             8
   :height            8
   :background-color  (if error?
                        colors/red
                        (if pressed?
                          colors/blue
                          colors/black-transparent))
   :border-radius     50
   :margin-horizontal 5})

(defn puk-indicator
  [error?]
  {:width             8
   :height            8
   :background-color  (if error?
                        colors/red
                        colors/black-transparent)
   :border-radius     50
   :margin-horizontal 5})

(def numpad-container
  {:margin-top 18})

(defn numpad-row-container
  [small-screen?]
  {:flex-direction  :row
   :justify-content :center
   :align-items     :center
   :margin-vertical (if small-screen? 4 10)})

(defn numpad-button
  [small-screen?]
  {:width             (if small-screen? 50 64)
   :margin-horizontal (if small-screen? 10 14)
   :height            (if small-screen? 50 64)
   :align-items       :center
   :justify-content   :center
   :flex-direction    :row
   :border-radius     (/ (if small-screen? 50 64) 2)
   :background-color  colors/blue-light})

(defn numpad-delete-button
  [small-screen?]
  (assoc (numpad-button small-screen?) :background-color colors/white))

(defn numpad-empty-button
  [small-screen?]
  (assoc (numpad-button small-screen?)
         :background-color colors/white
         :border-color     colors/white))

(def numpad-button-text
  {:font-size 22
   :color     colors/blue})
