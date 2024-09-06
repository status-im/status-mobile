(ns status-im.contexts.wallet.sheets.slippage-settings.style)

(def slippages
  {:padding-horizontal 8})

(def info-message
  {:margin-vertical   8
   :margin-horizontal 20})

(defn percentage-icon
  [variant-colors]
  {:size  20
   :color (:icon variant-colors)})
