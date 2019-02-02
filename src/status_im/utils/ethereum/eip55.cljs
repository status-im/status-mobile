(ns status-im.utils.ethereum.eip55
  "Utility function related to [EIP55](https://eips.ethereum.org/EIPS/eip-55)

   This EIP standardize how ethereum addresses should be printed as strings to validate checksum.

   e.g. 0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed"
  (:require [clojure.string :as string]
            [status-im.utils.ethereum.core :as ethereum]))

(defn valid-address-checksum? [address]
  "verify address checksum according to EIP 55"
  (let [adHash (ethereum/naked-address (ethereum/sha3
                                        (string/lower-case (ethereum/naked-address address))))]
    (every? true?
            (map-indexed (fn [idx char]
                           (if (> (compare char "9") 0)
                             (if (>= (js/parseInt (nth adHash idx) 16) 8)
                                ;  If true should be upper case
                               (<= (compare char "Z") 0)
                                ; If not should be lower case
                               (> (compare char "Z") 0))
                             true)) (ethereum/naked-address address)))))