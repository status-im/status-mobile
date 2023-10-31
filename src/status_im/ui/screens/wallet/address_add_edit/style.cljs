(ns status-im.ui.screens.wallet.address-add-edit.style)

(def container
  {:flex 1})
(def input
  {:margin-right 12
   :flex         1})
(def data-item
  {:margin-horizontal  20
   :padding-vertical   8
   :padding-horizontal 12})
(defn button-container
  [bottom]
  {:position :absolute
   :bottom   (+ bottom 12)
   :left     20
   :right    20})
