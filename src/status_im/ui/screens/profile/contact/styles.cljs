(ns status-im.ui.screens.profile.contact.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def network-info {:background-color :white})

(defstyle profile-info-item
  {:flex-direction :row
   :align-items    :center
   :padding-left   16})

(defn profile-info-text-container [options]
  {:flex          1
   :padding-right (if options 16 40)})

(defstyle profile-info-title
  {:color       colors/gray
   :font-size   14
   :ios         {:letter-spacing -0.2}})

(defstyle profile-setting-spacing
  {:ios     {:height 10}
   :android {:height 7}})

(defstyle profile-setting-text
  {:ios     {:font-size      17
             :letter-spacing -0.2}
   :android {:font-size 16
             :color     colors/black}})

(def action-container
  {:background-color colors/white
   :padding-top      24})

(def action
  {:background-color (colors/alpha colors/blue 0.1)
   :border-radius    50})

(def action-label
  {:color colors/blue})

(def action-separator
  {:height           1
   :background-color colors/gray-light
   :margin-left      50})

(def action-icon-opts
  {:color colors/blue})

(def profile-setting-text-empty
  (merge profile-setting-text
         {:color colors/gray}))

(defstyle contact-profile-info-container
  {:padding-top      26
   :background-color colors/white})

