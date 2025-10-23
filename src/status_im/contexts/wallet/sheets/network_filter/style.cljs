(ns status-im.contexts.wallet.sheets.network-filter.style)

(def header-container
  {:padding-bottom  4
   :flex-direction  :row
   :justify-content :space-between
   :align-items     :center})

(def item-container
  {:padding-horizontal 12
   :padding-top        12
   :padding-bottom     14
   :flex-direction     :row
   :justify-content    :space-between})

(def item-icon
  {:padding-top 1})

(def item-left-side
  {:flex            1
   :justify-content :flex-start
   :flex-direction  :row})

(def action-icon-container
  {:flex-direction :row
   :padding-right  0.5
   :align-items    :center})

(def new-gradient
  {:border-radius      12
   :height             16
   :padding-horizontal 4
   :margin-horizontal  4})

(def item-title-container
  {:flex-direction :row :align-items :center})

(def item-balances-container
  {:flex-direction :row
   :height         18})
