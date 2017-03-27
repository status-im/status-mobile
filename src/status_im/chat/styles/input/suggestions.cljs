(ns status-im.chat.styles.input.suggestions
  (:require [status-im.components.styles :as common]))

(def color-item-title-text "rgb(147, 155, 161)")
(def color-item-suggestion-name "rgb(98, 143, 227)")
(def color-item-border "#e8eaeb")

(defn item-title-container [top-padding?]
  {:margin-top  (if top-padding? 16 0)
   :margin-left 16})

(def item-title-text
  {:font-size 14
   :color     color-item-title-text})

(defn item-suggestion-container [border?]
  {:flex-direction      :row
   :padding-top         16
   :padding-bottom      16
   :margin-left         16
   :border-bottom-color color-item-border
   :border-bottom-width (if border? 1 0)})

(def item-suggestion-name
  {:background-color color-item-suggestion-name
   :height           26
   :border-radius    4
   :padding-left     6
   :padding-right    6
   :padding-top      4
   :padding-bottom   4})

(def item-suggestion-name-text
  {:color     common/color-white
   :font-size 14})

(def item-suggestion-description
  {:align-self   :stretch
   :flex         1
   :font-size    14
   :margin-left  16
   :margin-right 16
   :margin-top   4
   :color        color-item-title-text})