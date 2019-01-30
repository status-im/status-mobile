(ns status-im.ui.components.styles
  (:require [status-im.ui.components.colors :as colors]))

(def flex
  {:flex 1})

(def main-container
  {:background-color colors/white
   :flex             1})

(def modal
  {:position :absolute
   :left     0
   :top      0
   :right    0
   :bottom   0})

(def border-radius 8)

(def icon-default
  {:width  24
   :height 24})

(def text-title-bold
  {:font-size   17
   :font-weight "700"})

(def text-main
  {:font-size 15})

(def text-main-medium
  {:font-size   15
   :font-weight "500"})
