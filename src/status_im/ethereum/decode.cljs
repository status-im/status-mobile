(ns status-im.ethereum.decode
  (:require [status-im.ethereum.abi-spec :as abi-spec]))

(defn uint
  [hex]
  (first (abi-spec/decode hex ["uint"])))

(defn string
  [hex]
  (first (abi-spec/decode hex ["string"])))
