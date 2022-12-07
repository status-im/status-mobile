(ns status-im.ethereum.encode
  (:require [status-im.native-module.core :as status]))

(defn uint
  [x]
  (str "0x" (status/number-to-hex x)))
