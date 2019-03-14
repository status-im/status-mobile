(ns status-im.ui.screens.pairing.styles
  (:require
   [status-im.ui.components.styles :as styles]
   [status-im.ui.components.colors :as colors])
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]]))

(def wrapper
  {:flex             1
   :background-color :white})

(def installation-item-inner
  {:flex           1
   :flex-direction :row})

(defstyle installation-item
  {:flex-direction   :row
   :background-color :white
   :align-items      :center
   :ios              {:height 64}
   :android          {:height 56}})

(def installation-list
  {:background-color   :white
   :padding-horizontal 16
   :flex               1})

(def edit-installation
  {:padding-top        10
   :padding-horizontal 16})

(def footer-content {:justify-content :center
                     :flex            1
                     :align-items     :center})

(def footer-text {:color      colors/blue
                  :text-align :center})

(def pair-this-device
  {:height             80
   :padding-horizontal 16
   :padding-top        12
   :background-color   :white})

(def info-section
  {:padding-horizontal 16
   :padding-top        12})

(def info-section-text
  {:color colors/blue})

(def pair-this-device-actions
  {:flex           1
   :flex-direction :row})

(defn pairing-button [enabled?]
  {:width            40
   :height           40
   :background-color (if enabled?
                       colors/blue-light
                       colors/gray-lighter)
   :border-radius    28
   :align-items      :center
   :justify-content  :center})

(def installation-status
  {:color colors/gray})

(def pairing-actions-text
  {:flex        1
   :margin-left 16})

(def pair-this-device-title
  {:color         colors/blue
   :margin-bottom 6})

(defnstyle pairing-button-icon [enabled?]
  (let [color (if enabled?
                colors/blue
                colors/gray)]
    {:desktop {:tint-color color}
     :ios     {:color color}
     :android {:color color}}))

(def paired-devices-title
  {:color           colors/gray
   :margin-vertical 10})

(def bottom-container
  {:flex-direction    :row
   :margin-horizontal 12
   :margin-vertical   15})

(def input-container
  {:flex-direction    :row
   :align-items       :center
   :justify-content   :space-between
   :border-radius     styles/border-radius
   :height            52
   :margin-top        15})

(defstyle input
  {:flex    1
   :android {:padding 0}})
