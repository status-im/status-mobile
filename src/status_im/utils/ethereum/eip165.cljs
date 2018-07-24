(ns status-im.utils.ethereum.eip165
  "Utility function related to [EIP165](https://eips.ethereum.org/EIPS/eip-165)"
  (:require [status-im.utils.ethereum.core :as ethereum]))

(def supports-interface-hash "0x01ffc9a7")
(def marker-hash "0xffffffff")

(defn supports-interface? [web3 contract hash cb]
  (ethereum/call web3
                 (ethereum/call-params contract "supportsInterface(bytes4)" hash)
                 #(cb %1 %2)))

(defn supports?
  "Calls cb with true if `supportsInterface` is supported by this contract.
   See EIP for details."
  [web3 contract cb]
  (supports-interface? web3 contract supports-interface-hash
                       #(if (true? (ethereum/hex->boolean %2))
                          (supports-interface? web3 contract marker-hash
                                               (fn [o oo]
                                                 (cb o (false? (ethereum/hex->boolean oo)))))
                          (cb %1 false))))
