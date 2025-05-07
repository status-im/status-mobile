(ns quo.components.share.share-qr-code.style
  (:require [quo.foundations.colors :as colors]))

(def outer-container
  {:border-radius 16
   :width         "100%"
   :overflow      :hidden})

(def overlay-color colors/white-opa-5)

(def ^:private padding-horizontal 12)

(def content-container
  {:z-index            1
   :padding-horizontal padding-horizontal
   :padding-top        12
   :padding-bottom     8})

;;; QR code
(defn qr-code-size
  [total-width]
  (- total-width (* 2 padding-horizontal)))

(def share-qr-container
  {:flex-direction  :row
   :justify-content :space-between
   :margin-bottom   20})

(def share-qr-inner-container
  {:flex-direction :row
   :align-items    :center})

;;; Bottom part
(def bottom-container
  {:margin-top      8
   :flex-direction  :row
   :justify-content :space-between})

(def title {:color colors/white-opa-40})

(def share-button-size 32)
(def ^:private share-button-gap 16)

(defn share-button-container
  [alignment]
  {:justify-content (if (= alignment :top) :flex-start :center)
   :margin-left     share-button-gap})

(defn data-text
  [total-width]
  {:width (- total-width (* 2 padding-horizontal) share-button-size share-button-gap)})

;;; Wallet variants
(def watched-account-icon
  {:margin-left 4})
