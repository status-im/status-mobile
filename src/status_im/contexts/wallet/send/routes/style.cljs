(ns status-im.contexts.wallet.send.routes.style)

(def routes-container
  {:padding-horizontal 20
   :padding-vertical   16
   :width              "100%"
   :height             "100%"})

(def routes-header-container
  {:flex-direction  :row
   :justify-content :space-between})

(def routes-inner-container
  {:margin-top      8
   :flex-direction  :row
   :align-items     :center
   :justify-content :space-between})

(defn section-label
  [margin-left]
  {:flex        0.5
   :margin-left margin-left})

(def network-link
  {:right   6
   :z-index 1})
