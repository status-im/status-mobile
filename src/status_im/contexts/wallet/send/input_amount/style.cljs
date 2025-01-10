(ns status-im.contexts.wallet.send.input-amount.style)

(def screen
  {:flex 1})

(def input-container
  {:padding-top    12
   :padding-bottom 0})

(defn keyboard-container
  [bottom]
  {:padding-bottom bottom})

(def estimated-fees-container
  {:height             48
   :width              "100%"
   :flex-direction     :row
   :align-items        :center
   :padding-horizontal 20
   :padding-top        8})

(def estimated-fees-content-container
  {:align-items :center
   :height      40})

(def fees-data-item
  {:flex             1
   :height           40
   :background-color :transparent})

(def amount-data-item
  {:flex             1
   :height           40
   :background-color :transparent})

(def no-routes-found-container
  {:height      40
   :width       "100%"
   :align-items :center})
