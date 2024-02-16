(ns legacy.status-im.ui.screens.currency-settings.styles
  (:require
    [legacy.status-im.utils.styles :as styles]))

(styles/def currency-item
  {:flex-direction     :row
   :justify-content    :space-between
   :align-items        :center
   :padding-horizontal 16
   :ios                {:height 64}
   :android            {:height 56}})

(def currency-name-text
  {:typography :title})
