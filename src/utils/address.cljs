(ns utils.address
  ;; TODO move to status-im2
  (:require [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.eip55 :as eip55]))

(defn get-shortened-address
  "Takes first and last 4 digits from address including leading 0x
  and adds unicode ellipsis in between"
  [address]
  (when address
    (str (subs address 0 6) "\u2026" (subs address (- (count address) 3) (count address)))))

(defn get-shortened-checksum-address
  [address]
  (when address
    (get-shortened-address (eip55/address->checksum (ethereum/normalized-hex address)))))
