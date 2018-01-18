(ns status-im.ui.screens.wallet.request.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.styles :as styles]))

(def network-container
  {:flex        1
   :align-items :center})

(def qr-container
  {:margin-top       8
   :padding          16
   :background-color styles/color-white
   :border-radius    8})

(def share-icon-container
  {:margin-right 8})
