(ns status-im.contexts.wallet.swap.setup-swap.style)

(def container
  {:flex 1})

(def keyboard-container
  {:align-self :flex-end
   :width      "100%"})

(def inputs-container
  {:padding-top        12
   :padding-horizontal 20})

(def details-container
  {:flex-direction     :row
   :justify-content    :space-between
   :padding-top        7
   :padding-horizontal 20})

(def detail-item
  {:flex             1
   :height           36
   :background-color :transparent})

(def swap-order-button
  {:margin-top -9
   :z-index    2
   :align-self :center})

(def receive-token-swap-input-container
  {:margin-top -9})

(def footer-container
  {:flex            1
   :justify-content :flex-end})
