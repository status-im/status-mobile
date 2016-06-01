(ns status-im.qr-scanner.styles
  (:require [status-im.components.styles :refer [toolbar-height]]))

(def barcode-scanner-container
  {:flex            1
   :backgroundColor :white})

(def barcode-scanner
  {:flex 1
   :justifyContent :flex-end
   :alignItems :center})

(def rectangle-container
  {:position :absolute
   :left 0
   :top toolbar-height
   :bottom 0
   :right 0
   :flex 1
   :alignItems :center
   :justifyContent :center
   :backgroundColor :transparent})

(def rectangle
  {:height 250
   :width 250
   :backgroundColor :transparent})

(def corner-left-top
  {:position :absolute
   :left 0
   :top 0
   :width 56
   :height 56})

(def corner-right-top
  {:position :absolute
   :right 0
   :top 0
   :width 56
   :height 56})

(def corner-right-bottom
  {:position :absolute
   :right 0
   :bottom 0
   :width 56
   :height 56})

(def corner-left-bottom
  {:position :absolute
   :left 0
   :bottom 0
   :width 56
   :height 56})


;:width 56
;:height 56