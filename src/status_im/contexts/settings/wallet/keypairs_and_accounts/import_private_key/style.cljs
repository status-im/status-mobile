(ns status-im.contexts.settings.wallet.keypairs-and-accounts.import-private-key.style)

(def form-container
  {:row-gap            8
   :padding-top        8
   :padding-horizontal 20})

(def full-layout {:flex 1})

(defn page-container
  [insets]
  {:position :absolute
   :top      0
   :bottom   (:bottom insets)
   :left     0
   :right    0})

(def slide-container
  {:padding-horizontal 20
   :padding-vertical   12
   :margin-top         :auto
   :flex-direction     :row})

(def page-top
  {:margin-top 2})
