(ns status-im.contexts.wallet.sheets.network-preferences.style
  (:require [quo.foundations.colors :as colors]
            [react-native.platform :as platform]))

(def blur
  {:position      :absolute
   :top           0
   :left          0
   :right         0
   :bottom        0
   :overlay-color (when platform/ios? colors/neutral-100-opa-70-blur)})

(def data-item
  {:margin-horizontal 20
   :margin-vertical   8})

(defn warning-container
  [sending-to-unpreferred-networks?]
  {:flex-direction    :row
   :margin-horizontal 20
   :margin-bottom     (when sending-to-unpreferred-networks? 8)
   :align-items       :center})

(defn sending-to-unpreferred-networks-alert-container
  [theme]
  {:height            76
   :flex-direction    :row
   :background-color  (colors/resolve-color :blue theme 5)
   :border-color      (colors/resolve-color :blue theme 10)
   :border-width      1
   :border-radius     12
   :margin-horizontal 20
   :padding           10})

(def sending-to-unpreferred-networks-text
  {:flex        1
   :height      54.6
   :margin-left 8})
