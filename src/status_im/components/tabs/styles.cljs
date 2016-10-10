(ns status-im.components.tabs.styles
  (:require [status-im.components.styles :refer [color-white]]))

(def tabs-height 60)
(def tab-height 58)

(defn tabs-container [hidden? animation? offset-y]
  {:position        :absolute
   :bottom          0
   :left            0
   :right           0
   :height          tabs-height
   :backgroundColor color-white
   :marginBottom    (if (or hidden? animation?)
                      (- tabs-height) 0)
   :transform       [{:translateY (if animation? offset-y 1)}]})

(def bottom-gradient
  {:position :absolute
   :bottom 0
   :left 0
   :right 0})

(def tabs-inner-container
  {:flexDirection   :row
   :height          tab-height
   :opacity         1
   :backgroundColor :white
   :justifyContent  :center
   :alignItems      :center})

(def tab
  {:flex           1
   :height         tab-height
   :justifyContent :center
   :alignItems     :center})

(def tab-title
  {:font-size 12
   :height    16
   :color     "#6e93d8"})

(def tab-icon
  {:width        24
   :height       24
   :marginBottom 1})

(defn tab-container [active?]
  {:flex           1
   :height         tab-height
   :justifyContent :center
   :alignItems     :center
   :top            (if active? 0 8)})

(defn tab-view-container [offset-x]
  {:position       :absolute
   :top            0
   :left           0
   :right          0
   :bottom         0
   :padding-bottom 60
   :transform      [{:translateX offset-x}]})

(defn animated-offset [value]
  {:top value})
