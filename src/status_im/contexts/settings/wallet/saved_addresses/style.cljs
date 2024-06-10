(ns status-im.contexts.settings.wallet.saved-addresses.style)

(def title-container
  {:flex               0
   :padding-horizontal 20
   :margin-top         12
   :margin-bottom      16})

(defn page-wrapper
  [inset-top]
  {:padding-top inset-top
   :flex        1})

(def empty-container-style
  {:justify-content :center
   :flex            1})
