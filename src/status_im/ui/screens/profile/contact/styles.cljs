(ns status-im.ui.screens.profile.contact.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def network-info {:background-color :white})

(defstyle profile-setting-item
  {:flex-direction :row
   :align-items    :center
   :padding-left   16
   :ios            {:height 73}
   :android        {:height 72}})

(defn profile-info-text-container [options]
  {:flex          1
   :padding-right (if options 16 40)})

(defstyle profile-info-title
  {:color       colors/gray
   :margin-left 16
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

(def profile-setting-text-empty
  (merge profile-setting-text
         {:color colors/gray}))

(def profile-info-item-button
  {:padding 16})

