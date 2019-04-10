(ns status-im.utils.ethereum.eip165
  "Utility function related to [EIP165](https://eips.ethereum.org/EIPS/eip-165)"
  (:require [status-im.utils.ethereum.core :as ethereum]))

(def supports-interface-hash "0x01ffc9a7")
(def marker-hash "0xffffffff")

(defn supports-interface? [contract hash cb]
  (ethereum/call (ethereum/call-params contract "supportsInterface(bytes4)" hash)
                 #(cb %)))

(defn supports?
  "Calls cb with true if `supportsInterface` is supported by this contract.
   See EIP for details."
  [web3 contract cb]
  (supports-interface?
   contract
   supports-interface-hash
   #(if (true? (ethereum/hex->boolean %))
      (supports-interface? contract
                           marker-hash
                           (fn [response]
                             (cb (false? (ethereum/hex->boolean response)))))
      (cb false))))
