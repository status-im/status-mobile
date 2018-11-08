(ns status-im.ui.screens.group.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(defn toolbar-icon [enabled?]
  {:width   20
   :height  18
   :opacity (if enabled? 1 0.3)})

(def group-container
  {:flex             1
   :flex-direction   :column
   :background-color colors/white})

(def contact
  {:padding-left 0})

(def contacts-list
  {:background-color colors/white})

(def no-contact-text
  {:margin-bottom     20
   :margin-horizontal 50
   :text-align        :center
   :color             colors/gray})
