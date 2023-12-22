(ns status-im.contexts.preview-screens.quo-preview.buttons.wallet-ctas
  (:require
    [quo.core :as quo]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(defn view
  []
  [preview/preview-container {}
   [quo/wallet-ctas
    {:buy-action     #(js/alert "Buy button pressed")
     :send-action    #(js/alert "Send button pressed")
     :receive-action #(js/alert "Receive button pressed")
     :bridge-action  #(js/alert "Bridge button pressed")}]])
