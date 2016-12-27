(ns status-im.chat.styles.emoji
  (:require [status-im.components.styles :refer [color-white]]
            [status-im.chat.constants :refer [emoji-container-height]]))

(def container-height emoji-container-height)

(defn container [height]
  {:flexDirection   :column
   :position        :absolute
   :left            0
   :right           0
   :bottom          0
   :height          (or height emoji-container-height)
   :backgroundColor color-white
   :elevation       5})

(def emoji-container
  {:flex 1})

(def emoji-picker
  {:flex 1
   :background-color color-white})
