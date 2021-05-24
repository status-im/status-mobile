(ns status-im.ui.screens.onboarding.styles
  (:require [status-im.ui.components.colors :as colors]))

(def wizard-title
  {:margin-bottom 16
   :text-align    :center})

(defn wizard-text []
  {:color      colors/gray
   :text-align :center})

(def bottom-button
  {:padding-horizontal 24
   :justify-content    :center
   :align-items        :center
   :flex-direction     :row})

(def multiaccount-image
  {:width            40
   :height           40
   :border-radius    20
   :border-width     1
   :border-color     colors/black-transparent})

(defn list-item [selected?]
  {:flex-direction   :row
   :align-items      :center
   :justify-content  :space-between
   :padding-left     16
   :padding-right    10
   :background-color (if selected? colors/blue-light colors/white)
   :padding-vertical 12})