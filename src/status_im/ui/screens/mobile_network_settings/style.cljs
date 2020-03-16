(ns status-im.ui.screens.mobile-network-settings.style
  (:require [status-im.ui.components.colors :as colors]))

(def container
  {:flex             1})

(def switch-container
  {:height           52})

(def details
  {:margin-right     16
   :margin-left      16
   :margin-top       8
   :margin-bottom    16})

(def use-mobile-data-text
  {:color colors/gray})

(defn settings-separator []
  {:align-self       :stretch
   :height           1
   :background-color colors/gray-lighter})

(def defaults-container
  {:height          52
   :justify-content :center
   :padding-left    16})

(def defaults
  {:color colors/blue})
