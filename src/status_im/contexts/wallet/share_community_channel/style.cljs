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

(def qr-size 500)

(def qr-code-wrapper
  {:padding-horizontal 20
   :align-items        :center})

(defn qr-code-size
  [total-width]
  (- total-width 64))

(defn gradient-cover-size
  [total-width]
  (- total-width 40))

(defn community-share-wrapper
  [padding-top]
  {:padding-top padding-top})

(defn gradient-cover-wrapper
  [width]
  {:width         (gradient-cover-size width)
   :position      :absolute
   :border-radius 12
   :z-index       -1})
