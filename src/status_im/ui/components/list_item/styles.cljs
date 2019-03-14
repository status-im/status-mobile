(ns status-im.ui.components.list-item.styles
  (:require [status-im.ui.components.colors :as colors]))

(defn container [small?]
  {:height           (if small? 52 64)
   :align-items      :center
   :flex-direction   :row
   :padding-right    8
   :background-color :white})

(defn title [small? subtitle]
  (merge (when-not small?
           {:font-size 17})
         (when subtitle
           {:font-weight "500"})))

(def subtitle
  {:margin-top  4
   :color       colors/gray})

(def accessory-text
  {:color        colors/gray
   :margin-right 8})

(defn radius [size] (/ size 2))

(defn photo [size]
  {:border-radius (radius size)
   :width         size
   :height        size})
