(ns status-im.ui.screens.onboarding.welcome.styles
  (:require [status-im.ui.components.colors :as colors]))

(def welcome-view
  {:flex            1
   :justify-content :flex-end})

(def welcome-text
  {:typography :header
   :text-align :center})

(def welcome-text-description
  {:margin-top        16
   :margin-bottom     32
   :text-align        :center
   :margin-horizontal 40
   :color             colors/gray})