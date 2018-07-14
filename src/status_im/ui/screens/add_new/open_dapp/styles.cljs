(ns status-im.ui.screens.add-new.open-dapp.styles
  (:require [status-im.ui.components.colors :as colors]))

(def main-container
  {:flex             1
   :background-color colors/white})

(def gray-label
  {:color          colors/gray})

(def black-label
  {:font-size      15
   :letter-spacing -0.2})

(def dapp
  (merge gray-label
         {:margin-top 4}))

(def dapp-name
  (merge black-label
         {:margin-top 8}))

(def list-title
  {:margin-top     24
   :margin-left    16
   :font-size      14
   :letter-spacing -0.2
   :color          colors/gray})

(def description-container
  {:margin-top        26
   :margin-horizontal 16})
