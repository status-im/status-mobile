(ns status-im.chat.styles.input.result-box
  (:require [status-im.ui.components.styles :as common]
            [status-im.ui.components.colors :as colors]))

(defn root [height bottom]
  {:background-color common/color-white
   :border-top-color colors/gray-light
   :border-top-width 1
   :flex-direction   :column
   :height           height
   :left             0
   :right            0
   :elevation        2
   :bottom           bottom
   :position         :absolute})

