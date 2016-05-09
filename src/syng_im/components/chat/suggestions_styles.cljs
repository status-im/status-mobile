(ns syng-im.components.chat.suggestions-styles
  (:require [syng-im.components.styles :refer [font color-white]]))

(def suggestion-item-container
  {:flexDirection    "row"
   :marginVertical   1
   :marginHorizontal 0
   :height           40
   :backgroundColor  color-white})

(defn suggestion-background
  [{:keys [color]}]
  {:flexDirection   "column"
   :position        "absolute"
   :top             10
   :left            60
   :backgroundColor color
   :borderRadius    10})

(def suggestion-text
  {:marginTop -2
   :marginHorizontal 10
   :fontSize         14
   :fontFamily       font
   :color            color-white})

(def suggestion-description
  {:flex       1
   :position   "absolute"
   :top        7
   :left       190
   :lineHeight 18
   :fontSize   14
   :fontFamily font
   :color      "black"})

(defn suggestions-container
  [suggestions]
  {:flexDirection    "row"
   :marginVertical   1
   :marginHorizontal 0
   :height           (min 105 (* 42 (count suggestions)))
   :backgroundColor  color-white
   :borderRadius     5})
