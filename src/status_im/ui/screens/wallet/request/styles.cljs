(ns status-im.ui.screens.wallet.request.styles
  (:require [status-im.ui.components.colors :as colors]))

(def hint
  {:color colors/white-lighter-transparent})

(def footer
  {:color colors/white})

(def bottom-buttons
  {:background-color colors/blue})

(def request-wrapper
  {:flex              1
   :flex-direction    :column
   :margin-horizontal 16})

;; Request panel

(def request-details-wrapper
  {:padding-bottom 60})

(def send-request
  {:background-color colors/black-transparent
   :margin-bottom    12})
