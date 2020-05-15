(ns status-im.ethereum.contracts
  (:require [status-im.ethereum.core :as ethereum]))

(def contracts
  {:status/snt
   {:mainnet "0x744d70fdbe2ba4cf95131626614a1763df805b9e"
    :testnet "0xc55cf4b03948d7ebc8b9e8bad92643703811d162"}
   :status/tribute-to-talk
   {:testnet "0xC61aa0287247a0398589a66fCD6146EC0F295432"}
   :status/stickers
   {:testnet "0x8cc272396be7583c65bee82cd7b743c69a87287d"
    :mainnet "0x0577215622f43a39f4bc9640806dfea9b10d2a36"}
   :status/sticker-market
   {:testnet "0x6CC7274aF9cE9572d22DFD8545Fb8c9C9Bcb48AD"
    :mainnet "0x12824271339304d3a9f7e096e62a2a7e73b4a7e7"}
   :status/sticker-pack
   {:testnet "0xf852198d0385c4b871e0b91804ecd47c6ba97351"
    :mainnet "0x110101156e8F0743948B2A61aFcf3994A8Fb172e"}
   :status/acquisition
   {:rinkeby "0x75D370306139E22cBA52ec59408e9d85cAb9aa23"}})

(defn get-address
  [db contract]
  (let [chain-keyword (ethereum/chain-keyword db)]
    (get-in contracts [contract chain-keyword])))
