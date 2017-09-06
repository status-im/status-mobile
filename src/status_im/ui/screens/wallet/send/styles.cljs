(ns status-im.ui.screens.wallet.send.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.components.styles :as styles]))

(def wallet-container
  {:flex             1
   :background-color styles/color-blue2})

(def wallet-modal-container
  {:flex             1
   :background-color styles/color-blue4})

(def toolbar
  {:background-color styles/color-blue5
   :elevation        0
   :padding-bottom   10})

(def toolbar-title-container
  {:flex           1
   :flex-direction :row
   :margin-left    6})

(def toolbar-title-text
  {:color        styles/color-white
   :font-size    17
   :margin-right 4})

(def toolbar-icon
  {:width  24
   :height 24})

(def toolbar-title-icon
  (merge toolbar-icon {:opacity 0.4}))

(def toolbar-buttons-container
  {:flex-direction  :row
   :flex-shrink     1
   :justify-content :space-between
   :width           68
   :margin-right    12})

(def choose-recipient-container
  {:flex-direction  :row
   :padding-top     20
   :padding-bottom  20
   :justify-content :center})

(def choose-recipient-label
  {:color :white})

(def recipient-buttons
  {:flex-direction    :column
   :margin-horizontal 28
   :margin-vertical   10
   :border-radius     5
   :background-color  styles/color-blue5})

(def recipient-icon {:margin-right 20})

(def recipient-button
  {:flex-direction  :row
   :justify-content :space-between
   :margin-vertical 10
   :margin-left     20})

(def recipient-button-text
  {:color     :white
   :font-size 17})

(defn recipient-touchable [divider?]
  (cond-> {:border-color styles/color-gray-transparent-medium-light}
    divider? (assoc :border-bottom-width 1)))

(def qr-container
  {:flex              1
   :margin-horizontal 32})

(def preview
  {:flex            1
   :justify-content :flex-end
   :align-items     :center})

(defn outer-bezel [{:keys [height width]}]
  {:position      :absolute
   :top           -10
   :left          -10
   :border-radius 20
   :border-color  styles/color-blue2
   :border-width  20
   :height        (+ height 20)
   :width         (+ width 20)})

(defn inner-bezel [{:keys [height width]}]
  {:top           5
   :left          5
   :position      :absolute
   :border-radius 20
   :border-color  :white
   :border-width  5
   :height        (- height 10)
   :width         (- width 10)})
