(ns status-im.ui.components.list-item.styles
  (:require [status-im.ui.components.colors :as colors]))

(defn container [small?]
  {:height           (if small? 52 64)
   :align-items      :center
   :flex-direction   :row
   :padding-right    8
   :background-color :white})

(defn title [small? subtitle]
  (cond-> (if small?
            {:line-height 22 :font-size 15}
            {:line-height 20 :font-size 17})
    subtitle
    (assoc :font-weight "500" :font-size 15)))

(def subtitle
  {:margin-top  4
   :line-height 22
   :font-size   15
   :color       colors/gray})

(def accessory-text
  {:line-height  22
   :font-size    15
   :color        colors/gray
   :margin-right 8})

(defn radius [size] (/ size 2))

(defn photo [size]
  {:border-radius (radius size)
   :width         size
   :height        size})