(ns quo2.components.notifications.activity-log.style
  (:require [quo2.foundations.colors :as colors]))

(def container
  {:flex-direction :row
   :flex-grow      1
   :align-items    :flex-start
   :padding-top    8
   :padding-bottom 12})

(def icon
  {:height          32
   :width           32
   :border-radius   100
   :margin-top      10
   :border-width    1
   :border-color    colors/white-opa-5
   :align-items     :center
   :justify-content :center})

(def message-title
  {:color         colors/white-opa-40
   :margin-bottom 2})

(def message-body
  {:color colors/white})

(defn message-container
  [attachment]
  {:border-radius      12
   :margin-top         12
   :padding-horizontal 12
   :padding-vertical   (if (#{:photo :gif} attachment) 12 8)
   :background-color   colors/white-opa-5})

(def footer-container
  {:margin-top     12
   :flex-direction :row})

(defn title
  []
  {:color colors/white})

(def timestamp
  {:text-transform :none
   :margin-left    8
   :color          colors/neutral-40})

(defn unread-dot
  [customization-color]
  {:background-color (colors/custom-color (or customization-color :blue) 60)
   :border-radius    4
   :width            8
   :height           8})

(def unread-dot-container
  {:margin-left        8
   :padding-horizontal 12
   :padding-vertical   7})

(def context-container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :flex-start
   :flex-wrap       :wrap})

(def top-section-container
  {:align-items    :center
   :flex-direction :row})

(def title-container
  {:flex           1
   :flex-direction :row
   :align-items    :center})
