(ns status-im.ui.screens.chat.styles.input.validation-message
  (:require [status-im.ui.components.styles :as common]
            [status-im.ui.components.colors :as colors]))

(defn root [bottom]
  {:flex-direction :column
   :left           0
   :right          0
   :bottom         bottom
   :position       :absolute})

(def message-container
  {:background-color colors/red
   :padding          16})

(def message-title
  {:color     colors/white
   :font-size 12})

(def message-description
  {:color     colors/white
   :font-size 12
   :opacity   0.9})
