(ns status-im.contexts.browser.rpc-params
  (:require [utils.hex :as hex]))

(defn switch-ethereum-chain
  [event]
  (-> event :params first :chain-id hex/normalize-hex hex/hex-to-number))
