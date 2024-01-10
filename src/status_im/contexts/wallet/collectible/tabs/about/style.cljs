(ns status-im.contexts.wallet.collectible.tabs.about.style)

(def title
  {:padding-horizontal 20
   :padding-top        8
   :padding-bottom     4})

(def description
  {:padding-horizontal 20
   :padding-top        4
   :padding-bottom     12})

(def section-label
  {:margin-horizontal 20
   :margin-top        12})

(def link-cards-container
  {:margin-horizontal 20
   :margin-top        12
   :flex-direction    :row
   :justify-content   :space-between
   :flex-wrap         :wrap})

(defn link-card
  [item-width]
  {:margin-bottom 16
   :width         item-width})
