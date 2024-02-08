(ns quo.components.share.share-community-qr-code.style
  (:require [quo.foundations.colors :as colors]))

(def outer-container
  {:border-radius 16
   :width         "100%"
   :overflow      :hidden})

(def overlay-color colors/white-opa-5)

(def ^:private padding-horizontal 12)

(def content-container
  {:z-index            1
   :padding-horizontal padding-horizontal})

;;; QR code
(defn qr-code-size
  [total-width]
  (- total-width (* 2 padding-horizontal)))

(def share-qr-container
  {:padding-bottom 12})

(def share-qr-inner-container
  {:flex-direction :row
   :align-items    :center})
