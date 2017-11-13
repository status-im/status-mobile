(ns status-im.chat.styles.input.suggestions
  (:require-macros [status-im.utils.styles :refer [defnstyle]])
  (:require [status-im.ui.components.styles :as common]))

(def color-item-title-text "rgb(147, 155, 161)")
(def color-item-suggestion-name "rgb(98, 143, 227)")
(def color-item-border "#e8eaeb")

(defn item-title-container [top-padding?]
  {:margin-left      16
   :align-items      :center
   :flex-direction   :row
   :height           44})

(def item-title-text
  {:font-size 14
   :color     color-item-title-text})

(defnstyle item-suggestion-container [last?]
  {:flex-direction :row
   :align-items    :center
   :height         56
   :margin-left    16
   :ios            {:border-bottom-color color-item-border
                    :border-bottom-width (if last? 0 1)}})

(def item-suggestion-name
  {:background-color color-item-suggestion-name
   :height           28
   :flex-direction   :row
   :align-items      :center
   :border-radius    4
   :padding-left     7
   :padding-right    7})

(def item-suggestion-name-text
  {:color     common/color-white
   :font-size 14})

(def item-suggestion-description
  {:flex         1
   :font-size    14
   :margin-left  16
   :margin-right 16
   :color        color-item-title-text})