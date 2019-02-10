(ns status-im.ui.components.lists.cell.styles
  (:require [status-im.ui.components.styles :as common-styles]
            [status-im.ui.components.colors :as colors]))

(def cell-container
  {:height         64
   :align-self     :stretch
   :flex-direction :row})

(def icon-container
  {:width           72
   :justify-content :center
   :align-items     :center})

(defn- get-color [color]
  (get
   {:blue       colors/blue
    :red        colors/red
    :red-light  colors/red-light
    :blue-light colors/blue-light}
   color
   color))

(defn icon [color background-color]
  {:width           24
   :height          24
   :color           (get-color color)
   :container-style {:width            40
                     :height           40
                     :border-radius    20
                     :background-color (get-color background-color)
                     :justify-content  :center
                     :align-items      :center}})

(def description
  {:flex           1
   :padding-top    8
   :padding-bottom 8})

(def cell-text
  {:height          18
   :justify-content :center
   :margin-top      3
   :margin-bottom   3})

(defn item-title [color]
  (merge common-styles/text-main-medium
         {:color (get-color color)}))

(def item-details
  (merge common-styles/text-main
         {:color colors/gray}))
