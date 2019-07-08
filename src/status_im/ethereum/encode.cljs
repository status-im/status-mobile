(ns status-im.ethereum.encode
  (:require [status-im.js-dependencies :as dependencies]))

(defn utils [] (dependencies/web3-utils))

(defn uint
  [x]
  (.numberToHex (utils) x))
