(ns status-im.ethereum.decode
  (:require [status-im.utils.ethereum.abi-spec :as abi-spec]))

(defn uint
  [hex]
  (first (abi-spec/decode hex ["uint"])))
