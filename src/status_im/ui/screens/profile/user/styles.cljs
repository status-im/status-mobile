(ns status-im.ui.screens.profile.user.styles
  (:require [status-im.ui.components.colors :as colors]))

(def share-contact-code-container
  {:flex-direction  :row
   :justify-content :center
   :align-items     :center})

(def share-contact-code-button
  {:height 42})

(def qr-code
  {:background-color colors/white})

(def qr-code-viewer
  {:flex-grow      1
   :flex-direction :column})

(def share-link-button
  {:margin-top    12
   :margin-bottom 8})

(def advanced-button
  {:margin-top    16
   :margin-bottom 12})

(def advanced-button-container
  {:align-items     :center
   :justify-content :center})

(def advanced-button-container-background
  {:padding-left     16
   :padding-right    12
   :padding-vertical 6
   :border-radius    18
   :background-color colors/blue-transparent-10})

(def advanced-button-row
  {:flex-direction :row
   :align-items    :center})

(def advanced-button-label
  {:color colors/blue})

(def pair-button
  {:margin-left 32})
