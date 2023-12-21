(ns status-im.contexts.wallet.send.input-amount.style)

(def screen
  {:flex 1})

(def input-container
  {:padding-top    12
   :padding-bottom 0})

(defn keyboard-container
  [bottom]
  {:padding-bottom bottom})

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
