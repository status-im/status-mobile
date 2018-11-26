(ns status-im.ui.screens.desktop.main.styles
  (:require [status-im.ui.components.colors :as colors]))

(def main-views
  {:flex           1
   :flex-direction :row})

(def left-sidebar
  {:width            340
   :background-color colors/white})

(def pane-separator
  {:width            1
   :background-color colors/gray-light})

(def absolute
  {:position :absolute
   :top      0
   :right    0
   :left     0
   :bottom   0
   :flex     1})
