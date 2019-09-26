(ns status-im.ui.screens.hardwallet.pin.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.styles :as styles]))

(def container
  {:flex             1
   :background-color colors/white})

(styles/def pin-container
  {:flex            1
   :flex-direction  :column
   :justify-content :space-between})

(styles/defn error-container [small-screen?]
  {:height        (if small-screen? 18 22)
   :margin-top    (if small-screen? 14 10)
   :margin-bottom (if small-screen? 10 0)})

(defn error-text [small-screen?]
  {:color      colors/red
   :font-size  (if small-screen? 12 15)
   :text-align :center})

(defn center-container [title]
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
   :margin-top      16})

(def pin-indicator-group-container
  {:flex-direction  :row
   :justify-content :space-between})

(defn pin-indicator [pressed? status]
  {:width             8
   :height            8
   :background-color  (if (= status :error)
                        colors/red
                        (if pressed?
                          colors/blue
                          colors/black-transparent))
   :border-radius     50
   :margin-horizontal 5})

(def waiting-indicator-container
  {:margin-top 26})

(def numpad-container
  {:margin-top 18})

(defn numpad-row-container [small-screen?]
  {:flex-direction  :row
   :justify-content :center
   :align-items     :center
   :margin-vertical (if small-screen? 4 10)})

(defn numpad-button [small-screen?]
  {:width             (if small-screen? 50 64)
   :margin-horizontal (if small-screen? 10 14)
   :height            (if small-screen? 50 64)
   :align-items       :center
   :justify-content   :center
   :flex-direction    :row
   :border-radius     50
   :background-color  colors/blue-light})

(defn numpad-delete-button [small-screen?]
  (assoc (numpad-button small-screen?) :background-color colors/white))

(defn numpad-empty-button [small-screen?]
  (assoc (numpad-button small-screen?) :background-color colors/white
         :border-color colors/white))

(def numpad-button-text
  {:font-size 22
   :color     colors/blue})

(def numpad-empty-button-text
  {:color colors/white})
