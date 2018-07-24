(ns status-im.utils.ethereum.stateofus
  (:require [clojure.string :as string]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.ens :as ens]))

(defn name [web3 registry name cb]
  (ens/resolver web3 registry name
                #(ens/name web3 %2 name cb)))
