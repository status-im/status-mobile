(ns status-im.utils.ethereum.stateofus
  (:refer-clojure :exclude [name])
  (:require [clojure.string :as string]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.ens :as ens]))

(defn is-stateofus-name? [ens-name]
  (string/ends-with? ens-name ".stateofus.eth"))

(defn addr [web3 registry ens-name cb]
  {:pre [(is-stateofus-name? ens-name)]}
  (ens/resolver web3
                registry
                ens-name
                #(ens/addr web3 % ens-name cb)))

(defn text
  "calls the text function on the stateofus resolver contract for `statusAccount` key
  TODO: https://solidity.readthedocs.io/en/develop/abi-spec.html needs to be implemented
  to replace this by dynamic parameters"
  [web3 registry ens-name cb]
  {:pre [(is-stateofus-name? ens-name)]}
  (ens/resolver web3
                registry
                ens-name
                #(ethereum/call web3
                                {:to %
                                 :data (str "0x59d1d43c"
                                            (subs (ens/namehash ens-name) 2)
                                            "0000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000000d7374617475734163636f756e7400000000000000000000000000000000000000")}
                                (fn [_ text] (cb (ethereum/hex->string (subs text 130 214)))))))

#_(addr (:web3 @re-frame.db/app-db) "0x112234455c3a32fd11230c42e7bccd4a84e02010" "test.stateofus.eth" println)
#_(text (:web3 @re-frame.db/app-db) "0x112234455c3a32fd11230c42e7bccd4a84e02010" "test.stateofus.eth" println)
