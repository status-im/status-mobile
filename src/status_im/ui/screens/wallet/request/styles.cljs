(ns status-im.ui.screens.wallet.request.styles
  (:require [status-im.ui.components.colors :as colors]))

(def hint
  {:color colors/white-transparent})

(def footer
  {:color colors/white
   :border-color colors/white-transparent-10})

(def bottom-buttons
  {:background-color colors/blue})

(def request-wrapper
  {:flex              1
   :flex-direction    :column})

;; Request panel

(def request-details-wrapper
  {:padding-bottom 60})

(def send-request
  {:background-color  colors/black-transparent
   :margin-top        12
   :margin-bottom     16
   :margin-horizontal 16
   :height            42})
