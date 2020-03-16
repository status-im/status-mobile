(ns status-im.ui.screens.profile.photo-capture.styles
  (:require [status-im.ui.components.colors :as colors]))

(def container
  {:flex             1
   :background-color colors/white})

(def button-container
  {:align-items :center})

(def button
  {:position :absolute
   :bottom   10})