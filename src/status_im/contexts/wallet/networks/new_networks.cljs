(ns status-im.contexts.wallet.networks.new-networks
  (:require [clojure.set :as set]
            [promesa.core :as promesa]
            [react-native.async-storage :as async-storage]))

(defn get-stored-seen
  []
  (some->
    (async-storage/get-item :networks/seen-new-networks)
    (promesa/then set)))

(defn set-stored-seen
  [chain-ids]
  (async-storage/set-item! :networks/seen-new-networks chain-ids))

(defn marked-as-seen?
  [chain-ids]
  (some->
    (get-stored-seen)
    (promesa/then #(set/subset? chain-ids %))))

(defn mark-as-seen
  [chain-ids]
  (promesa/-> (get-stored-seen)
              (into chain-ids)
              (set-stored-seen)))
