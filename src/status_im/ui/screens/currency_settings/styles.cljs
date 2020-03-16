(ns status-im.ui.screens.currency-settings.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.styles :as styles]))

(def wrapper
  {:flex             1
   :background-color colors/white})

(styles/def currency-item
  {:flex-direction     :row
   :justify-content    :space-between
   :align-items        :center
   :padding-horizontal 16
   :ios                {:height 64}
   :android            {:height 56}})

(def currency-name-text
  {:typography :title})
