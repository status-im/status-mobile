(ns status-im.contexts.wallet.send.transaction-progress.style)

(def content-container
  {:flex              1
   :margin-horizontal 20})

(def footer
  {:flex-direction  :row
   :justify-content :space-between
   :width           "100%"})

(defn footer-button
  [save-address-visible?]
  {:width (if save-address-visible? "48%" "100%")})
