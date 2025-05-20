(ns status-im.contexts.wallet.sheets.account-options.style)

(def drawer-section-label
  {:padding-horizontal 20
   :padding-top        12
   :padding-bottom     8})

(def options-container
  {:position :absolute
   :top      0
   :left     0
   :right    0
   :z-index  1
   :overflow :hidden})

(defn blur-container
  [height]
  {:height   height
   :position :absolute
   :top      0
   :left     0
   :right    0})

(def divider-label
  {:margin-top 8})

(defn list-container
  [padding-top]
  {:padding-top       padding-top
   :margin-horizontal 8})
