(ns status-im.ui.screens.add-new.open-dapp.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def main-container {:flex 1})

(def enter-url
  {:flex-direction    :row
   :align-items       :center
   :border-radius     8
   :height            52
   :background-color  colors/light-gray
   :margin-horizontal 14
   :margin-top        24})

(defstyle url-input
  {:flex               1
   :font-size          15
   :letter-spacing     -0.2
   :padding-horizontal 14
   :android            {:padding 0}})

(def gray-label
  {:font-size      14
   :letter-spacing -0.2
   :color          colors/gray})

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