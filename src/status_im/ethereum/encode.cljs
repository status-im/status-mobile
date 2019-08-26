(ns status-im.ethereum.encode
  (:require [status-im.js-dependencies :as dependencies]))

(def utils dependencies/web3-utils)

(defn uint
  [x]
  (.numberToHex utils x))
