(ns status-im.ui.components.contact.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.styles :as common]
            [status-im.utils.platform]
            [status-im.ui.components.colors :as colors]))

(def contact-container-to-refactor
  {:flex-direction :row
   :justify-content :center
   :align-items :center
   :padding-horizontal 16})

(def info-container-to-refactor
  {:flex        1
   :flex-direction  :column
   :margin-left 16
   :justify-content :center})

(def info-container
  {:flex            1
   :justify-content :center})

(def name-text
  {:color     colors/text
   :font-size 17})

(def info-text
  {:margin-top 1
   :font-size  12
   :color      colors/text-gray})

(def forward-btn
  {:opacity         0.4
   :padding         12
   :align-items     :center
   :justify-content :center})

(def more-btn-container
  {:align-items     :center
   :justify-content :center})

(def more-btn
  {:padding 16})

(def toggle-container
  {:width          56
   :height         56
   :alignItems     :center
   :justifyContent :center})

(def check-icon
  {:width  16
   :height 16})
