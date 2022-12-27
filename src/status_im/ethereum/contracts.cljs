(ns status-im.ethereum.contracts
  (:require [status-im.ethereum.core :as ethereum]))

(def contracts
  {:status/snt
   {:mainnet "0x744d70fdbe2ba4cf95131626614a1763df805b9e"
    :goerli  "0x3D6AFAA395C31FCd391fE3D562E75fe9E8ec7E6a"}
   :status/stickers
   {:mainnet "0x0577215622f43a39f4bc9640806dfea9b10d2a36"
    :goerli  "0x07f7CB0C0a4ab3e0999AfE8b3997Da34880f05d0"}
   :status/sticker-market
   {:mainnet "0x12824271339304d3a9f7e096e62a2a7e73b4a7e7"
    :goerli  "0xf1E149A7DF70D5Ff1E265daAa738d785D3274717"}
   :status/sticker-pack
   {:mainnet "0x110101156e8F0743948B2A61aFcf3994A8Fb172e"
    :goerli  "0x8D3fD2EA24bD53a8Bd2b1026727db8bbe9A8C8Af"}})

(defn get-address
  [db contract]
  (let [chain-keyword (ethereum/chain-keyword db)]
    (get-in contracts [contract chain-keyword])))
