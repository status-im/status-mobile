(ns status-im.contexts.settings.wallet.network-settings.style)

(def title-container
  {:padding-vertical   12
   :padding-horizontal 20
   :flex-direction     :row
   :justify-content    :space-between
   :align-items        :center})

(defn page-wrapper
  [inset-top]
  {:padding-top inset-top
   :flex        1})

(defn settings-container
  [inset-bottom]
  {:padding-bottom (+ inset-bottom 8)})

(def networks-container
  {:padding-top 4})

(def advanced-settings-container
  {:padding-bottom 4})
