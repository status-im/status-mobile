(ns status-im.ui.screens.chat.styles.input.suggestions
  (:require-macros [status-im.utils.styles :refer [defnstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def item-height 52)
(def border-height 1)

(def root
  {:background-color colors/white
   :border-top-color colors/gray-light
   :border-top-width 1})

(def item-suggestion-container
  {:flex-direction      :row
   :align-items         :center
   :height              item-height
   :padding-horizontal  14
   :border-top-color colors/gray-light
   :border-top-width border-height})

(def item-suggestion-description
  {:flex        1
   :margin-left 10
   :color       colors/gray})
