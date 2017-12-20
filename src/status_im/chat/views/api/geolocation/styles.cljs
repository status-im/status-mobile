(ns status-im.chat.views.api.geolocation.styles
  (:require [status-im.ui.components.styles :as common]))

(defn place-item-container [address]
  {:height          (if address 74 52)
   :justify-content :center})

(def place-item-title-container
  {:flex-direction :row
   :align-items    :center})

(defn place-item-circle-icon [pin-style]
  (merge {:border-color  common/color-light-blue
          :border-width  3
          :border-radius 7
          :height        13
          :width         13}
         pin-style))

(def black-pin
  {:border-color  common/color-black})

(def place-item-title
  {:font-size     15
   :padding-left  9
   :padding-right 16
   :color         common/color-black
   :line-height   23})

(def place-item-address
  {:font-size     15
   :padding-left  22
   :padding-right 16
   :color         common/color-black
   :line-height   23})

(def map-activity-indicator-container
  {:align-items     :center
   :justify-content :center
   :height          100})

(def map-view
  {:height 100})

(def location-container
  {:margin-top        11
   :margin-horizontal 16})

(def location-container-title
  {:font-size      14
   :color          common/color-gray4
   :letter-spacing -0.2})

(def location-container-title-count
  (merge location-container-title
         {:opacity 0.5}))

(def separator
  {:height           1
   :opacity          0.5
   :background-color "#c1c7cbb7"})

(def item-separator
  (merge separator
         {:margin-left 22}))

(def pin-container
  {:position        :absolute
   :top             0
   :right           0
   :bottom          0
   :left            0
   :justify-content :center
   :align-items     :center
   :pointer-events  :none})

(def pin-component
  {:align-items :center})

(def pin-circle
  {:border-color     common/color-black
   :background-color common/color-white
   :border-width     3
   :border-radius    7
   :height           13
   :width            13})

(def pin-leg
  {:height            7
   :width             2
   :background-color  common/color-black})