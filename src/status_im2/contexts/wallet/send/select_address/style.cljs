(ns status-im2.contexts.wallet.send.select-address.style)

(defn container
  [margin-top]
  {:flex       1
   :margin-top margin-top})

(def title-container
  {:margin-horizontal 20
   :margin-vertical   12})

(def tabs
  {:padding-top    20
   :padding-bottom 12})

(def tabs-content
  {:padding-left  20
   :padding-right 8})

(def empty-container-style
  {:justify-content :center
   :flex            1
   :margin-bottom   44})
