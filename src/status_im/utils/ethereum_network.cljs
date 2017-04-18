(ns status-im.utils.ethereum-network
  (:require [status-im.constants :as c]
            [status-im.utils.web-provider :as w3]))

(def Web3 (js/require "web3"))

(defn web3 []
  (Web3. (w3/get-provider c/ethereum-rpc-url)))

(def networks
  {"0xd4e56740f876aef8c010b86a40d5f56745a118d0906a34e69aec8c0db1cb8fa3" :mainnet
   "0x0cd786a2425d16f152c658316c423e6ce1181e15c3295826d7c9904cba9ce303" :testnet
   ;; Ropsten
   "0x41941023680923e0fe4d74a34bdac8141f2540e3ae90623718e47d66d1ca4a2d" :testnet})

(defn- on-block [callback]
  (fn [error block]
    (when-not error
      (let [hash (.-hash block)]
        (callback (networks hash :unknown))))))

(defn get-network [callback]
  (.eth.getBlock (web3) 0 false (on-block callback)))
