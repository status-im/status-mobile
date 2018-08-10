(ns status-im.ui.screens.wallet.transaction-sent.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def transaction-sent-container
  {:align-items :center})

(def ok-icon-container
  {:width            56
   :height           56
   :border-radius    28
   :background-color :white
   :align-items      :center
   :justify-content  :center
   :margin-top       57
   :margin-bottom    16})

(def transaction-sent
  {:color     :white
   :font-size 17})

(def gap
  {:height 8})

(def transaction-sent-description
  {:color              :white
   :opacity            0.6
   :text-align         :center
   :padding-horizontal 16})

(def transaction-details-container
  {:height            42
   :background-color  colors/black-darker-transparent
   :margin-horizontal 16
   :opacity           0.2
   :align-items       :center
   :justify-content   :center
   :border-radius     8})

(def transaction-details
  {:color     :white
   :font-size 15})

(def got-it-container
  {:align-items      :center
   :padding-vertical 18})

(def got-it
  {:color     :white
   :font-size 15})
