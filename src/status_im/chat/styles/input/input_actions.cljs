(ns status-im.chat.styles.input.input-actions
  (:require-macros [status-im.utils.styles :refer [defnstyle]])
  (:require [status-im.components.styles :as common]))

(def actions-container
  {:flex-direction :row
   :margin-left    10})

(defn action-view [enabled?]
  {:width           38
   :height          38
   :opacity         (if enabled? 1 0.5)
   :justify-content :center
   :align-items     :center})

(def action-view-icon
  {:width  24
   :height 24})

(def action-view-icon-tinted
  {:width      24
   :height     24
   :tint-color "black"})

(def action-view-fullscreen-expand-icon
  {:width  16
   :height 16})