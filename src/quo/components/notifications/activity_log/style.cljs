(ns quo.components.notifications.activity-log.style
  (:require
    [quo.foundations.colors :as colors]))

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
  [attachment text-with-photos?]
  {:border-radius      12
   :margin-top         10
   :padding-horizontal 12
   :padding-vertical   (if (and
                            (not text-with-photos?)
                            (#{:photo :gif} attachment))
                         12
                         8)
   :background-color   colors/white-opa-5})

(def footer-container
  {:margin-top     12
   :flex-direction :row})

(defn title
  []
  {:flex-shrink 1
   :color       colors/white})

(def timestamp
  {:text-transform :none
   :margin-left    8
   :margin-top     4
   :flex-shrink    0
   :color          colors/neutral-40})

(defn unread-dot
  [customization-color]
  {:background-color (colors/resolve-color (or customization-color :blue) nil)
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
  {:flex-direction :row})

(def title-container
  {:flex           1
   :flex-direction :row})
