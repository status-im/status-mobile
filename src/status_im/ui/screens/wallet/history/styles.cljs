(ns status-im.ui.screens.wallet.history.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle]])
  (:require [status-im.components.styles :as common]))

(def wallet-transactions-container
  {:flex             1
   :background-color common/color-white})

(def toolbar-buttons-container
  {:flex-direction  :row
   :flex-shrink     1
   :justify-content :space-between
   :width           68
   :margin-right    12})

(def item
  {:flex-direction  :row
   :flex            1})

(def item-text-view
  {:flex            1
   :flex-direction  :column})

(def primary-text
  {:flex      1
   :font-size 16
   :color     common/color-black})

(def secondary-text
  {:font-size 16
   :color     common/color-gray4})

(def item-icon
  {:width  40
   :height 40})

(def secondary-action
  (merge item-icon {:align-self "flex-end"}))

;;;;;;;;;;;;;;;;;;
;; Main section ;;
;;;;;;;;;;;;;;;;;;

(def main-section
  {:padding 16
   :background-color common/color-white})