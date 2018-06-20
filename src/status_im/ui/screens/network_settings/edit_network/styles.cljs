(ns status-im.ui.screens.network-settings.edit-network.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as styles]))

(def edit-network-view
  {:flex              1
   :margin-horizontal 16
   :margin-vertical   15})

(def input-container
  {:margin-bottom 15})

(def network-type
  {:flex-direction :row
   :align-items    :center})

(defstyle network-type-text
  {:color   colors/black
   :ios     {:font-size      17
             :letter-spacing -0.2}
   :android {:font-size 16}})

(def bottom-container
  {:flex-direction    :row
   :margin-horizontal 12
   :margin-vertical   15})
