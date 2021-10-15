(ns status-im.ui.screens.profile.visibility-status.styles
  (:require [quo.design-system.colors :as colors]))

(def color-error "#FA6565")
(def color-online "#7CDA00")
(def color-dnd "#FA6565")
(def color-inactive "#939BA1")

(defn visibility-status-button-container []
  {:background-color       (:interactive-02 @colors/theme)
   :margin-left            16
   :border-radius          16
   :border-top-left-radius 4
   :align-self             "flex-start"
   :flex-direction         "row"
   :align-items            "center"
   :justify-content        "center"
   :padding                6
   :padding-right          12})

(defn visibility-status-dot [dot-color size]
  {:background-color dot-color
   :width            size
   :height           size
   :border-radius    (/ size 2)
   :border-width     1
   :border-color     "#FFFFFF"})

(defn visibility-status-profile-dot [color size border-width margin-left]
  (merge (visibility-status-dot color size)
         {:margin-right 6
          :margin-left  margin-left
          :border-width border-width}))

(defn visibility-status-text []
  {:color      (:text-01 @colors/theme)
   :font-size  16
   :weight     700
   :text-align "center"})

(defn visibility-status-subtitle []
  {:color        (:text-02 @colors/theme)
   :font-size    16
   :margin-left  22
   :margin-right 6
   :weight       600})

(defn visibility-status-option []
  {:flex-direction  "row"
   :align-items "center"})

(defn visibility-status-options [scale position]
  {:background-color       (:ui-background @colors/theme)
   :border-radius          16
   :border-top-left-radius 4
   :justify-content        "center"
   :align-self             "flex-start"
   :left                    16
   :top                    -76
   :padding-bottom          6
   :padding-top             8
   :transform               [{:scaleY scale}
                             {:translateY position}]})

(def visibility-status-popover-view
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})
