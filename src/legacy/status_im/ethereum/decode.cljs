(ns legacy.status-im.ethereum.decode
  (:require
    [utils.money :as money]))

(defn uint
  [hex]
  (let [n (money/bignumber hex)]
    (when n
      (.toString n 10))))
