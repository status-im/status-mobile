(ns status-im.ui.screens.profile.group-chat.styles
  (:require [status-im.ui.components.colors :as colors]))

(def action-container
  {:background-color colors/white
   :padding-top      24})

(def action
  {:background-color (colors/alpha colors/blue 0.1)
   :border-radius    50})

; Action label style is expected to be a fn
(defn action-label [_]
  {:color colors/blue})

(def action-separator
  {:height           1
   :background-color colors/gray-light
   :margin-left      50})

(def action-icon-opts
  {:color colors/blue})
