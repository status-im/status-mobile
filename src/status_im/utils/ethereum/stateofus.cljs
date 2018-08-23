(ns status-im.utils.ethereum.stateofus
  (:refer-clojure :exclude [name])
  (:require [clojure.string :as string]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.ens :as ens]))

(defn is-valid-name? [ens-name]
  (string/ends-with? ens-name ".stateofus.eth"))

(defn addr [web3 registry ens-name cb]
  {:pre [(is-valid-name? ens-name)]}
  (ens/resolver web3
                registry
                ens-name
                #(ens/addr web3 % ens-name cb)))

(defn pubkey
  [web3 registry ens-name cb]
  {:pre [(is-valid-name? ens-name)]}
  (ens/resolver web3
                registry
                ens-name
                #(ens/pubkey web3 % ens-name cb)))

#_(addr (:web3 @re-frame.db/app-db) "0x112234455c3a32fd11230c42e7bccd4a84e02010" "qweqwe.stateofus.eth" println)
#_(pubkey (:web3 @re-frame.db/app-db) "0x112234455c3a32fd11230c42e7bccd4a84e02010" "qweqwe.stateofus.eth" println)
