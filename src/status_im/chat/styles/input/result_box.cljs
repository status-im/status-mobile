(ns status-im.chat.styles.input.result-box
  (:require [status-im.ui.components.styles :as common]))

(def color-root-border "rgba(192, 198, 202, 0.5)")

(defn root [height bottom]
  {:background-color common/color-white
   :border-top-color color-root-border
   :border-top-width 1
   :flex-direction   :column
   :height           height
   :left             0
   :right            0
   :elevation        2
   :bottom           bottom
   :position         :absolute})

