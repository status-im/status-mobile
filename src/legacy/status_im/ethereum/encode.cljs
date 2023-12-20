(ns legacy.status-im.ethereum.encode
  (:require
    [native-module.core :as native-module]))

(defn uint
  [x]
  (str "0x" (native-module/number-to-hex x)))
