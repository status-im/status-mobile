(ns status-im.ethereum.contracts
  (:require [status-im.ethereum.core :as ethereum]))

(def contracts
  {:status/snt
   {:mainnet "0x744d70fdbe2ba4cf95131626614a1763df805b9e"
    :testnet "0xc55cf4b03948d7ebc8b9e8bad92643703811d162"}
   :status/tribute-to-talk
   {:testnet "0x3da3fc53e24707f36c5b4433b442e896c4955f0e"}
   :status/stickers
   {:testnet "0x39d16CdB56b5a6a89e1A397A13Fe48034694316E"}})

(defn get-address
  [db contract]
  (let [chain-keyword (ethereum/chain-keyword db)]
    (get-in contracts [contract chain-keyword])))
