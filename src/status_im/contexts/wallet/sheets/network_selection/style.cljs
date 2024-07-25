(ns status-im.contexts.wallet.sheets.network-selection.style)

(defn network-list-container
  [mainnet?]
  {:margin-horizontal 8
   :padding-vertical  (when mainnet? 8)})

(def header-container
  {:height             62
   :padding-horizontal 20})

(def context-tag
  {:margin-top 4})

(def divider-label
  {:padding-top    0
   :padding-bottom 0})
