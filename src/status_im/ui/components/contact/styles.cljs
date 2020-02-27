(ns status-im.ui.components.contact.styles
  (:require [status-im.ui.components.colors :as colors]))

(def contact-container
  {:flex-direction     :row
   :justify-content    :center
   :align-items        :center
   :padding-vertical   12
   :padding-horizontal 16})

(def info-container-to-refactor
  {:flex        1
   :flex-direction  :column
   :margin-left 16
   :justify-content :center})

(def info-container
  {:flex            1
   :justify-content :center})

(defn name-text []
  {:color     colors/gray
   :font-size 17})

(defn info-text []
  {:margin-top 1
   :font-size  12
   :color      colors/gray})

(def forward-btn
  {:opacity         0.4
   :padding         12
   :align-items     :center
   :justify-content :center})

(def toggle-container
  {:width          56
   :height         56
   :alignItems     :center
   :justifyContent :center})

(def check-icon
  {:width  16
   :height 16})
