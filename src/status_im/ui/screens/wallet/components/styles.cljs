(ns status-im.ui.screens.wallet.components.styles
  (:require [status-im.ui.components.colors :as colors]))

(def recent-recipients
  {:flex             1
   :background-color colors/white})

(defn separator []
  {:height           1
   :background-color colors/gray-lighter})