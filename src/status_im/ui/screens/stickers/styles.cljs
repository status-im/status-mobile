(ns status-im.ui.screens.stickers.styles
  (:require [status-im.ui.components.colors :as colors]))

(def screen
  {:flex             1})

(defn sticker-image [sticker-icon-size]
  {:margin        16
   :width         sticker-icon-size
   :height        sticker-icon-size
   :border-radius (/ sticker-icon-size 2)})

(defn price-badge [not-enough-snt?]
  {:background-color   (if not-enough-snt? colors/gray colors/blue)
   :border-radius      14
   :flex-direction     :row
   :padding-horizontal 6
   :height             28
   :align-items        :center})

(def installed-icon
  {:height           28
   :width            28
   :border-radius    14
   :background-color colors/green
   :align-items      :center
   :justify-content  :center})