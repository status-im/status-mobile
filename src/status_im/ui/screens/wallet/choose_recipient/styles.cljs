(ns status-im.ui.screens.wallet.choose-recipient.styles
  (:require [status-im.ui.components.colors :as colors]))

(def toolbar
  {:background-color :transparent})

(def wallet-container
  {:flex             1
   :background-color colors/blue})

(def recipient-button
  {:flex-direction  :row
   :justify-content :space-between
   :margin-vertical 10
   :margin-left     20})

(def recipient-button-text
  {:color      :white
   :align-self :center
   :font-size  14})

(def qr-container
  {:position :absolute
   :top      0
   :left     0
   :right    0
   :bottom   0})

(def corner-dimensions
  {:position :absolute
   :width    40
   :height   40})

(defn corner-left-bottom [height width size]
  (merge corner-dimensions {:bottom (int (/ (- height size) 2))
                            :left   (int (/ (- width size) 2))}))

(defn corner-right-bottom [height width size]
  (merge corner-dimensions {:right  (int (/ (- width size) 2))
                            :bottom (int (/ (- height size) 2))}))

(defn corner-left-top [height width size]
  (merge corner-dimensions {:top  (int (/ (- height size) 2))
                            :left (int (/ (- width size) 2))}))

(defn corner-right-top [height width size]
  (merge corner-dimensions {:top   (int (/ (- height size) 2))
                            :right (int (/ (- width size) 2))}))

(def viewfinder-port {:position :absolute
                      :left     0
                      :top      0
                      :bottom   0
                      :right    0
                      :flex     1})

(defn viewfinder-translucent [height width size side]
  (let [top-bottom-width  width
        top-bottom-height (int (/ (- height size) 2))
        left-right-width  (int (/ (- width size) 2))
        left-right-height (- height (* 2 top-bottom-height))]
    (cond-> {:position         :absolute
             :background-color :black
             :opacity          0.7}
      (= :top side)    (assoc :height top-bottom-height
                              :width  top-bottom-width)
      (= :right side)  (assoc :height left-right-height
                              :width  left-right-width
                              :top    top-bottom-height
                              :right  0)
      (= :bottom side) (assoc :height top-bottom-height
                              :width  top-bottom-width
                              :bottom 0
                              :left   0)
      (= :left side)   (assoc :height left-right-height
                              :width  left-right-width
                              :top    top-bottom-height
                              :left   0))))

(def qr-code
  {:flex             1
   :background-color colors/white-lighter-transparent
   :align-items      :center})

(defn qr-code-text [dimensions]
  {:zIndex      1
   :padding-top 20
   :color       :white
   :text-align  :center
   :width       (int (/ (:width dimensions) 2))})
