(ns legacy.status-im.ui.screens.profile.components.styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]))

;; profile header elements

;; settings items elements

(def settings-item
  {:padding-left   16
   :padding-right  8
   :flex           1
   :flex-direction :row
   :align-items    :center
   :height         52})

(def settings-item-icon
  {:background-color colors/blue-light
   :width            34
   :height           34
   :border-radius    34
   :margin-right     16
   :justify-content  :center
   :align-items      :center})

(def settings-item-text-wrapper
  {:flex            1
   :flex-direction  :row
   :justify-content :space-between})

(def settings-item-text
  {:flex-wrap :nowrap})

(def settings-item-destructive
  {:color colors/red})

(def settings-item-disabled
  {:color colors/gray})

(def settings-item-value
  {:flex          1
   :flex-wrap     :nowrap
   :text-align    :right
   :padding-right 10
   :color         colors/gray})

;; shared profile styles

(def profile
  {:flex           1
   :flex-direction :column})

(def profile-form
  {:padding-vertical 16})

;; sheets

(def sheet-text
  {:text-align  :center
   :line-height 22
   :font-size   15})
