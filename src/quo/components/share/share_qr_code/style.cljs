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
  {:margin-bottom 12})

(def header-title-container
  {:margin-bottom   20
   :flex-direction  :row
   :justify-content :space-between
   :overflow        :hidden})

(def header-and-avatar-container
  {:flex-direction :row
   :align-items    :center
   :word-wrap      :no-wrap})

(def flex-direction-row
  {:flex-direction :row})

(def header-tab-active {:background-color colors/white-opa-20})
(def header-tab-inactive {:background-color colors/white-opa-5})
(def space-between-tabs {:width 8})

;;; QR code
(defn qr-code-size
  [total-width]
  (- total-width (* 2 padding-horizontal)))

;;; Bottom part
(def bottom-container
  {:margin-top      8
   :flex-direction  :row
   :justify-content :space-between})

(def title {:color colors/white-opa-40})

(defn header-title
  [component-width]
  {:color       colors/white
   :margin-left 8
   :max-width   (* component-width 0.7)})

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
(def wallet-data-and-share-container
  {:margin-top      2
   :flex-direction  :row
   :align-items     :center
   :justify-content :space-between})

(def wallet-legacy-container {:flex 1})

(def wallet-multichain-container {:flex 1})

(def ^:private get-network-full-name
  {"eth"  :ethereum
   "opt"  :optimism
   "arb1" :arbitrum})

(defn network-short-name-text
  [network-short-name]
  {:color (-> network-short-name
              (get-network-full-name :unknown)
              (colors/resolve-color nil))})
