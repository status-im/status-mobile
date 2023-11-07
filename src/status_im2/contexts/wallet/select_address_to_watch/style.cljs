(ns status-im2.contexts.wallet.select-address-to-watch.style)

(def header-container
  {:margin-horizontal 20
   :margin-top        12
   :margin-bottom     20})

(def input-container
  {:flex-direction     :row
   :padding-horizontal 20
   :align-items        :flex-end})

(defn button-container
  [bottom]
  {:position :absolute
   :bottom   (- bottom 22)
   :left     20
   :right    20})
