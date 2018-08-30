(ns status-im.utils.ethereum.resolver
  (:require [status-im.utils.ethereum.ens :as ens]))

(def default-address "0x0000000000000000000000000000000000000000")
(def default-hash "0x0000000000000000000000000000000000000000000000000000000000000000")

(defn content [web3 registry ens-name cb]
  (ens/resolver web3
                registry
                ens-name
                (fn [address]
                  (if (and address (not= address default-address))
                    (ens/content web3 address ens-name cb)
                    (cb nil)))))