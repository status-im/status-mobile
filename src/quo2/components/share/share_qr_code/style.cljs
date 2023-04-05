(ns quo2.components.share.share-qr-code.style
  (:require [quo2.foundations.colors :as colors]))

(def qr-code-container
  {:padding-top        12
   :padding-horizontal 12
   :padding-bottom     8
   :border-radius      16
   :background-color   colors/white-opa-5
   :flex-direction     :column
   :justify-content    :center
   :align-items        :center})

(def profile-address-column
  {:margin-horizontal :auto
   :flex              4})

(def profile-address-container
  {:flex-direction  :row
   :justify-content :space-between
   :margin-top      6})

(def profile-address-content-container
  {:padding-top 2
   :align-self  :flex-start})

(def profile-address-content
  {:color colors/white})

(def profile-address-label
  {:color colors/white-opa-40})

(def share-button-container
  {:flex            1
   :flex-direction  :column
   :justify-content :center
   :align-items     :flex-end})

(def icon-container
  {:height          36
   :flex-direction  :row
   :align-items     :center
   :justify-content :space-between
   :padding-bottom  12})

