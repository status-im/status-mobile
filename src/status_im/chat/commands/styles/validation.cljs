(ns status-im.chat.commands.styles.validation
  (:require [status-im.ui.components.styles :as common]))

(defn root [bottom]
  {:flex-direction :column
   :left           0
   :right          0
   :bottom         bottom
   :position       :absolute})

(def message-container
  {:background-color common/color-red
   :padding          16})

(def message-title
  {:color     common/color-white
   :font-size 12})

(def message-description
  {:color     common/color-white
   :font-size 12
   :opacity   0.9})
