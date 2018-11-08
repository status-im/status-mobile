(ns status-im.ui.screens.pairing.styles
  (:require [status-im.ui.components.colors :as colors])
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

(defstyle installation-item-name-text
  {:color colors/black})

(def installation-list
  {:background-color   :white
   :padding-horizontal 16
   :flex               1})

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
   :font-size   15
   :margin-left 16})

(def pair-this-device-title
  {:color         colors/blue
   :margin-bottom 6
   :font-size     15})

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
