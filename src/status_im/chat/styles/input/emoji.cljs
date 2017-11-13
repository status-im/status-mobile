(ns status-im.chat.styles.input.emoji
  (:require [status-im.ui.components.styles :as common]))

(def container-height 250)

(defn container [height]
  {:flex-direction   :column
   :height           (or height container-height)
   :background-color common/color-white})

(def emoji-container
  {:flex 1})

(def emoji-picker
  {:flex             1
   :background-color common/color-white})
