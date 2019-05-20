(ns status-im.utils.ethereum.eip165
  "Utility function related to [EIP165](https://eips.ethereum.org/EIPS/eip-165)"
  (:require [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.ethereum.abi-spec :as abi-spec]))

(def supports-interface-hash "0x01ffc9a7")
(def marker-hash "0xffffffff")

(defn supports-interface?
  [contract hash cb]
  (json-rpc/eth-call
   {:contract contract
    :method "supportsInterface(bytes4)"
    :params [hash]
    :on-success cb}))

(defn supports?
  "Calls cb with true if `supportsInterface` is supported by this contract.
   See EIP for details."
  [web3 contract cb]
  (supports-interface?
   contract
   supports-interface-hash
   #(if (true? (abi-spec/hex-to-boolean %))
      (supports-interface? contract
                           marker-hash
                           (fn [response]
                             (cb (false? (abi-spec/hex-to-boolean response)))))
      (cb false))))
