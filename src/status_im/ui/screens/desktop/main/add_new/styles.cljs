(ns status-im.ui.screens.desktop.main.add-new.styles
  (:require [status-im.ui.components.colors :as colors]))

(def new-contact-view
  {:flex             1
   :background-color colors/white
   :margin-left      24
   :margin-right     37})

(def new-contact-title
  {:height          64
   :align-items     :flex-start
   :justify-content :center})

(def new-contact-title-text
  {:font-size   20
   :color       :black})

(def new-contact-subtitle
  {:font-size 14})

(def new-contact-separator
  {:height           1
   :background-color colors/gray-light})

(def add-contact-edit-view
  {:height           45
   :margin-bottom    32
   :background-color colors/white
   :border-radius    12
   :flex-direction   :row
   :margin-top       16})

(def add-contact-input
  {:font-size        14
   :background-color colors/gray-lighter
   :margin-right     12
   :border-radius    8})

(defn add-contact-button [error?]
  {:width            140
   :height           45
   :border-radius    8
   :background-color (if error? colors/gray-lighter colors/blue)
   :align-items      :center
   :justify-content  :center})

(defn add-contact-button-text [error?]
  {:font-size 16
   :color     (if error? colors/gray colors/white)})

(def suggested-contact-view
  {:flex-direction "row"
   :align-items    :center
   :margin-bottom  16})

(def suggested-contacts
  {:margin-top 12})

(def suggested-contact-image
  {:width         46
   :height        46
   :border-radius 46
   :margin-right  16})

(def suggested-topic-image
  (merge suggested-contact-image
         {:background-color colors/blue
          :align-items      :center
          :justify-content  :center}))

(def suggested-topic-text
  {:font-size 25.6
   :color     colors/white})
