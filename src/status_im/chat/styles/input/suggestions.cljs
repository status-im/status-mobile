(ns status-im.chat.styles.input.suggestions
  (:require-macros [status-im.utils.styles :refer [defnstyle]])
  (:require [status-im.ui.components.styles :as common]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform]))

(def item-height 52)
(def border-height 1)

(def root
  {:background-color common/color-white
   :border-top-color colors/gray-light
   :border-top-width 1})

(def item-suggestion-container
  {:flex-direction      :row
   :align-items         :center
   :height              item-height
   :padding-horizontal  14
   :border-bottom-color colors/gray-light
   :border-bottom-width border-height})

(def item-suggestion-name
  {:color     common/color-black
   :font-size 15})

(def item-suggestion-description
  {:flex        1
   :font-size   15
   :margin-left 10
   :color       colors/gray})
