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

;;; Header
(def header-container
  {:flex-direction :row
   :margin-bottom  12})

(def header-tab-active {:background-color colors/white-opa-20})
(def header-tab-inactive {:background-color colors/white-opa-5})
(def space-between-tabs {:width 8})

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
(def wallet-multichain-container
  {:flex-direction  :row
   :justify-content :space-between
   :flex            1})

;;; Dashed line
(def ^:private padding-for-divider (+ padding-horizontal 4))
(def ^:private dashed-line-width 2)
(def ^:private dashed-line-space 4)

(def dashed-line
  {:flex-direction :row
   :margin-left    -1})

(def line
  {:background-color colors/white-opa-20
   :width            dashed-line-width
   :height           1})

(def line-space
  {:width  dashed-line-space
   :height 1})

(defn number-lines-and-spaces-to-fill
  [component-width]
  (let [line-and-space-width (+ dashed-line-width dashed-line-space)
        width-to-fill        (- component-width (* 2 padding-for-divider))
        number-of-lines      (* (/ width-to-fill line-and-space-width) 2)]
    (inc (int number-of-lines))))

(def ^:private get-network-full-name
  {"eth"  :ethereum
   "opt"  :optimism
   "arb1" :arbitrum})

(defn network-short-name-text
  [network-short-name]
  {:color (-> network-short-name
              (get-network-full-name :unknown)
              (colors/resolve-color nil))})
