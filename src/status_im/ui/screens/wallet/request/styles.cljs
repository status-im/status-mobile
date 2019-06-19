(ns status-im.ui.screens.wallet.request.styles
  (:require [status-im.ui.components.colors :as colors]))

(def hint
  {:color colors/white-transparent
   :margin-top 11
   :margin-bottom 24
   :font-size 16
   :max-width 140
   :text-align :center})

(def footer
  {:color colors/white
   :border-color colors/white-light-transparent
   :margin-top 29
   :max-width 235})

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
