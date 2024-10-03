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

(defn swap-order-button
  [approval-required?]
  {:margin-top (if approval-required? 3 -9)
   :z-index    2
   :align-self :center})

(defn receive-token-swap-input-container
  [approval-required?]
  {:margin-top (if approval-required? 3 -9)})

(def footer-container
  {:flex            1
   :justify-content :flex-end})

(def alert-banner
  {:height     :auto
   :min-height 40
   :max-height 62})
