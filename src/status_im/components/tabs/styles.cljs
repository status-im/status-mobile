(ns status-im.components.tabs.styles
  (:require [status-im.components.styles :refer [color-white]]))

(def tabs-height 56)
(def tab-height (- tabs-height 1))

(def bottom-gradient
  {:position :absolute
   :bottom   55
   :left     0
   :right    0})

(defn tabs-container [hidden?]
  {:position         :absolute
   :bottom           0
   :left             0
   :right            0
   :height           tabs-height
   :background-color color-white
   :margin-bottom    (if hidden? (- tabs-height) 0)
   :transform        [{:translateY 1}]})

(def tabs-container-line
  {:border-top-width 1
   :border-top-color "#D7D7D7"})

(def tabs-inner-container
  {:flexDirection   :row
   :height          tab-height
   :opacity         1
   :justifyContent  :center
   :alignItems      :center})

(def tab
  {:flex           1
   :height         tab-height
   :justifyContent :center
   :alignItems     :center})

(def tab-title
  {:font-size  12
   :height     16
   :min-width  60
   :text-align :center
   :color      "#6e93d8"})

(def tab-icon
  {:width        24
   :height       24
   :marginBottom 1
   :align-self   :center})

(defn tab-container [active?]
  {:flex             1
   :height           tab-height
   :justifyContent   :center
   :alignItems       :center
   :padding-top      (if active? 0 16)})

(defn animated-offset [value]
  {:top value
   :justifyContent :center
   :alignItems     :center})

(def main-swiper
  {:position         :absolute
   :top              0
   :left             0
   :right            0
   :bottom           tabs-height
   :shows-pagination false})
