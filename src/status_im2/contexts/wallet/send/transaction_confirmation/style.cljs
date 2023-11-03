(ns status-im2.contexts.wallet.send.transaction-confirmation.style)

(defn container
  [margin-top]
  {:flex       1
   :margin-top margin-top})

(def title-container
  {:margin-horizontal 20
   :margin-vertical   12})

(def empty-container-style
  {:justify-content :center
   :flex            1
   :margin-bottom   44})

(def search-input-container
  {:padding-horizontal 20
   :padding-vertical   8})
