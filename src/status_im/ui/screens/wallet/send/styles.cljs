(ns status-im.ui.screens.wallet.send.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.components.styles :as styles]
            [status-im.utils.platform :as platform]))

(def wallet-container
  {:flex             1
   :background-color styles/color-blue4})

(def wallet-modal-container
  {:flex             1
   :background-color styles/color-blue3})

(def toolbar
  {:background-color styles/color-blue4
   :elevation        0
   :padding-bottom   10})

(def toolbar-title-container
  {:flex           1
   :flex-direction :row
   :margin-left    6})

(def toolbar-title-text
  {:color        styles/color-white
   :font-size    17
   :margin-right 4})

(def toolbar-icon
  {:width  24
   :height 24})

(def toolbar-title-icon
  (merge toolbar-icon {:opacity 0.4}))

(def toolbar-buttons-container
  {:flex-direction  :row
   :flex-shrink     1
   :justify-content :space-between
   :width           68
   :margin-right    12})

(def choose-recipient-container
  {:flex-direction  :row
   :padding-top     20
   :padding-bottom  20
   :justify-content :center})

(def choose-recipient-label
  {:color :white})

(defstyle recipient-buttons
  {:flex-direction    :column
   :margin-horizontal 28
   :margin-vertical   20
   :border-radius     8
   :ios {:background-color styles/color-blue6}})

(def recipient-icon {:margin-right 20})

(def recipient-icon-disabled {:margin-right 20
                              :opacity 0.3})

(def recipient-button
  {:flex-direction  :row
   :justify-content :space-between
   :margin-vertical 10
   :margin-left     20})

(def recipient-button-text
  {:color      :white
   :align-self :center
   :font-size  14})

(def recipient-button-text-disabled
  (merge recipient-button-text {:color "rgba(255, 255, 255, 0.3)"}))

(defnstyle recipient-touchable [divider?]
  (cond-> {:border-color styles/color-gray-transparent-light}
    divider? (assoc :ios {:border-bottom-width 1})))

(def recipient-touchable-disabled
  {:background-color           styles/color-blue4
   :border-bottom-left-radius  8
   :border-bottom-right-radius 8
   :border-left-width          1
   :border-bottom-width        1
   :border-right-width         1
   :border-color               "rgba(255, 255, 255, 0.3)"})

(def qr-container
  {:flex              1})

(def preview
  {:flex            1
   :justify-content :flex-end
   :align-items     :center})

(def corner-dimensions
  {:position :absolute
   :width    40
   :height   40})

(defn corner-left-bottom [dimension]
  (let [viewport-offset 0.1666]
    (merge corner-dimensions {:bottom (* viewport-offset dimension)
                              :left   (* viewport-offset dimension)})))

(defn corner-right-bottom [dimension]
  (let [viewport-offset 0.1666]
    (merge corner-dimensions {:right  (* viewport-offset dimension)
                              :bottom (* viewport-offset dimension)})))

(defn corner-left-top [dimension]
  (let [viewport-offset 0.1666]
    (merge corner-dimensions {:top  (* viewport-offset dimension)
                              :left (* viewport-offset dimension)})))

(defn corner-right-top [dimension]
  (let [viewport-offset 0.1666]
    (merge corner-dimensions {:top   (* viewport-offset dimension)
                              :right (* viewport-offset dimension)})))

(def viewfinder-port {:position :absolute
                      :left     0
                      :top      0
                      :bottom   0
                      :right    0
                      :flex     1})

(defn viewfinder-translucent [height width side]
  (let [viewport-offset 0.1666
        height-offset (* viewport-offset height)
        width-offset (* viewport-offset width)]
    (cond-> {:position         :absolute
             :background-color :black
             :opacity          0.7}
      (= :top side) (assoc :height height-offset
                           :width  width)
      (= :right side) (assoc :height (- height height-offset)
                             :width width-offset
                             :bottom 0
                             :right 0)
      (= :bottom side) (assoc :height height-offset
                              :width (- width width-offset)
                              :bottom 0
                              :left 0)
      (= :left side) (assoc :height (- height (* 2 height-offset))
                            :width width-offset
                            :top height-offset
                            :left 0))))
