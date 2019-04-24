(ns status-im.utils.ethereum.eip55
  "Utility function related to [EIP55](https://eips.ethereum.org/EIPS/eip-55)

   This EIP standardize how ethereum addresses should be printed as strings to validate checksum.

   e.g. 0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed"
  (:require [clojure.string :as string]
            [status-im.js-dependencies :as dependencies]))

(def utils dependencies/web3-utils)

(defn address->checksum
  "Converts an arbitrary case address to one with correct checksum case."
  [address]
  (when address
    (.toChecksumAddress utils address)))

(defn valid-address-checksum?
  "Checks address checksum validity."
  [address]
  (.checkAddressChecksum utils address))