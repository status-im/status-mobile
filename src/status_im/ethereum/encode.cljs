(ns status-im.ethereum.encode
  (:require ["web3-utils" :as utils]))

(defn uint
  [x]
  (.numberToHex utils x))
