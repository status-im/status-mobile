(ns status-im.contexts.settings.keycard.style
  (:require
    [quo.foundations.colors :as colors]))

(def registered-keycards-container
  {:gap                12
   :padding-horizontal 20})

(def keycard-row
  {:gap              12
   :padding          12
   :border-radius    16
   :flex-direction   :row
   :background-color colors/white-opa-5})

(def keycard-owner
  {:gap            4
   :flex-direction :row
   :align-items    :center})

(def keycard-owner-name
  {:color colors/white-opa-40})
