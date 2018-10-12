(ns status-im.utils.ethereum.resolver
  (:require [status-im.utils.ethereum.ens :as ens])
  (:refer-clojure :exclude [name]))

(def default-hash "0x0000000000000000000000000000000000000000000000000000000000000000")

(defn content [web3 registry ens-name cb]
  (ens/resolver web3
                registry
                ens-name
                #(ens/content web3 % ens-name cb)))

(defn name [web3 registry ens-name cb]
  (ens/resolver web3
                registry
                ens-name
                #(ens/name web3 % ens-name cb)))

(defn pubkey
  [web3 registry ens-name cb]
  {:pre [(ens/is-valid-eth-name? ens-name)]}
  (ens/resolver web3
                registry
                ens-name
                #(ens/pubkey web3 % ens-name cb)))
