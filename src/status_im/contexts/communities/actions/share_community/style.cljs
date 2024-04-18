(ns status-im.contexts.communities.actions.share-community.style
  (:require [quo.foundations.colors :as colors]))

(def header-container
  {:padding-horizontal 20
   :padding-vertical   12})

(def scan-notice
  {:color        colors/white-70-blur
   :margin-top   16
   :margin-left  :auto
   :margin-right :auto})

(defn community-name
  [thumbnail]
  {:margin-left (when thumbnail 8)})

(def qr-code-wrapper
  {:padding-horizontal 20
   :margin-top         8})

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
   :border-radius 16
   :margin-left   20
   :height        "100%"})

(def share-button-container {:justify-self :flex-end})

(def qr-top-wrapper
  {:margin          12
   :margin-bottom   0
   :flex-direction  :row
   :align-items     :center
   :justify-content :space-between})

(def community-avatar
  {:border-radius 32
   :width         32
   :height        32})
