(ns status-im.contexts.wallet.share-community-channel.style
  (:require [quo.foundations.colors :as colors]))

(def header-container
  {:padding-horizontal 20
   :padding-vertical   12})

(def scan-notice
  {:color        colors/neutral-40
   :margin-top   12
   :margin-left  :auto
   :margin-right :auto})

(def qr-code-wrapper
  {:padding-horizontal 20
   :align-items        :center})

(defn qr-code-size
  [total-width]
  (- total-width (* 2 32)))

(defn gradient-cover-size
  [total-width]
  (- total-width (* 2 20)))
