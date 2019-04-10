(ns status-im.ui.screens.qr-scanner.styles
  (:require-macros [status-im.utils.styles :as styles])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.toolbar.styles :as toolbar.styles]))

(def barcode-scanner-container
  {:flex             1
   :background-color :white})

(styles/defstyle barcode-scanner
  {:flex      1
   :elevation -10
   :android {:marginTop 10}})

(def rectangle-container
  {:position         :absolute
   :left             0
   :top              toolbar.styles/toolbar-height
   :bottom           0
   :right            0
   :flex             1
   :align-items      :center
   :justify-content  :center
   :background-color :transparent})

(def rectangle
  {:height           250
   :width            250
   :background-color :transparent})

(def corner-left-top
  {:position :absolute
   :left     0
   :top      0
   :width    56
   :height   56})

(def corner-right-top
  {:position :absolute
   :right    0
   :top      0
   :width    56
   :height   56})

(def corner-right-bottom
  {:position :absolute
   :right    0
   :bottom   0
   :width    56
   :height   56})

(def corner-left-bottom
  {:position :absolute
   :left     0
   :bottom   0
   :width    56
   :height   56})

(def import-button
  {:position    :absolute
   :right       16
   :flex        1
   :height      50
   :align-items :center})

(def import-button-content
  {:flex           1
   :flex-direction :row
   :height         50
   :align-items    :center
   :align-self     :center})

(def import-text
  {:flex           1
   :flex-direction :column
   :color          colors/white
   :margin-left    8})
