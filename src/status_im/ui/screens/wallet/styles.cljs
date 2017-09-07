(ns status-im.ui.screens.wallet.styles
  (:require [status-im.components.styles :as st]))

(def wallet-exclamation-container
  {:background-color st/color-red-2
   :justify-content  :center
   :margin-top       5
   :margin-left      10
   :margin-right     7
   :margin-bottom    5
   :border-radius    100})

(def wallet-error-exclamation
  {:width  16
   :height 16})
