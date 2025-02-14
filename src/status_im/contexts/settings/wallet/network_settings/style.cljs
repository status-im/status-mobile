(ns status-im.contexts.settings.wallet.network-settings.style)

(def title-container
  {:flex               0
   :padding-horizontal 20
   :padding-vertical   12})

(defn page-wrapper
  [inset-top]
  {:padding-top inset-top
   :flex        1})

(defn settings-container
  [inset-bottom]
  {:flex           1
   :padding-bottom inset-bottom})

(def networks-container
  {:flex 1})

(def advanced-settings-container
  {:flex-shrink 0})
