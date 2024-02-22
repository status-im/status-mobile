(ns status-im.contexts.communities.actions.share-community-channel.style
  (:require [quo.foundations.colors :as colors]))

(def header-container
  {:padding-horizontal 20
   :padding-vertical   12})

(def scan-notice
  {:color        colors/white-70-blur
   :margin-top   16
   :margin-left  :auto
   :margin-right :auto})

(def qr-code-wrapper
  {:padding-horizontal 20
   :margin-top         8
   :align-items        :center})

(def gradient-cover-padding 20)
(def qr-code-padding 12)

(defn qr-code-size
  [total-width]
  (- total-width (* gradient-cover-padding 2) (* qr-code-padding 2)))

(defn gradient-cover-size
  [total-width]
  (- total-width (* gradient-cover-padding 2)))

(defn gradient-cover-wrapper
  [width]
  {:width         (gradient-cover-size width)
   :position      :absolute
   :border-radius 12
   :z-index       -1})
