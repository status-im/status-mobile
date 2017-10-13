(ns status-im.ui.screens.wallet.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.components.styles :as styles]))

;; errors

(defstyle error-container
  {:flex-direction :row
   :align-items    :center
   :ios            {:padding-top    8
                    :padding-bottom 8}
   :android        {:padding-top    10
                    :padding-bottom 10}})

(def error-exclamation
  {:background-color styles/color-red-2
   :border-radius    100
   :width            16
   :height           16
   :margin-left      12
   :margin-right     6
   :margin-top       2})

(def warning-exclamation
  {:background-color :darkorange
   :border-radius    100
   :width            16
   :height           16
   :margin-left      12
   :margin-right     6
   :margin-top       2})

;; wallet

(def wallet-container
  {:flex 1})

(defstyle toolbar
  {:ios     {:background-color styles/color-blue4}
   :android {:background-color styles/color-blue5
             :elevation        0}})

(def buttons-container
  {:margin-vertical    15
   :padding-horizontal 12
   :flex-direction     :row
   :align-items        :center})

(def forward-icon-container
  {:margin-left 8})

(defn button-container [enabled?]
  (merge
    {:flex-direction :row
     :align-items    :center}
    (when-not enabled?
      {:opacity 0.4})))

(def wallet-modal-container
  {:flex             1
   :background-color styles/color-blue4})

(def choose-participant-container
  {:margin-top        16
   :margin-horizontal 15})

(def choose-wallet-container
  {:margin-top        16
   :margin-horizontal 15})

(def amount-container
  {:margin-top        16
   :margin-horizontal 15
   :flex-direction    :row})

(def choose-currency-container
  {:margin-left 8})

(def choose-currency
  {:width 116})
