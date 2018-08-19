(ns status-im.ui.screens.wallet.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

;; wallet

(def toolbar
  {:background-color colors/blue})

(defstyle toolbar-bottom-line
  {:ios {:border-bottom-width 1
         :border-bottom-color colors/white-transparent}})

(defn button-container [disabled?]
  (merge {:flex-direction :row
          :align-items    :center}
         (when disabled?
           {:opacity 0.4})))

(def wallet-modal-container
  {:flex             1
   :background-color colors/blue})
