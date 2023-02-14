(ns utils.address
  ;; TODO move to status-im2
  (:require [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.eip55 :as eip55]))

(defn get-shortened-key
  "Takes first and last 4 digits from address including leading 0x
  and adds unicode ellipsis in between"
  [value]
  (when value
    (str (subs value 0 6) "\u2026" (subs value (- (count value) 3) (count value)))))

(defn get-shortened-checksum-address
  [address]
  (when address
    (get-shortened-key (eip55/address->checksum (ethereum/normalized-hex address)))))
