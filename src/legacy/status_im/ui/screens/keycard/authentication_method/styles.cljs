(ns legacy.status-im.ui.screens.keycard.authentication-method.styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]))

(def container
  {:flex             1
   :background-color colors/white})

(def lock-image-container
  {:background-color colors/blue-light
   :width            160
   :height           160
   :border-radius    80
   :align-items      :center
   :justify-content  :center})

(def lock-image
  {:width  160
   :height 160})

(def choose-authentication-method
  {:flex-direction  :column
   :flex            1
   :align-items     :center
   :justify-content :center})

(def choose-authentication-method-text
  {:typography         :header
   :margin-top         51
   :padding-horizontal 60
   :text-align         :center})

(def choose-authentication-method-row-text
  {:color      colors/blue
   :text-align :center
   :font-size  17})

(def authentication-methods
  {:padding-vertical 24})

(def authentication-method-row
  {:padding-horizontal 16
   :flex-direction     :row
   :align-items        :center
   :height             63})

(def authentication-method-row-icon-container
  {:background-color colors/blue-light
   :width            40
   :height           40
   :border-radius    50
   :align-items      :center
   :justify-content  :center})

(def authentication-method-row-wrapper
  {:flex               1
   :flex-direction     :row
   :padding-horizontal 16
   :justify-content    :space-between})
