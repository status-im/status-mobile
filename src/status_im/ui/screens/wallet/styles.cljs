(ns status-im.ui.screens.wallet.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.components.styles :as st]
            [status-im.components.styles :as styles]))

(def wallet-container
  {:flex 1})

(defstyle toolbar
          {:ios     {:background-color styles/color-blue4}
           :android {:background-color styles/color-blue5
                     :elevation        0}})

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