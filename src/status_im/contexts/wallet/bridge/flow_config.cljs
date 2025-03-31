(ns status-im.contexts.wallet.bridge.flow-config
  (:require [status-im.contexts.wallet.db-path :as db-path]))

(def steps
  [{:screen-id  :screen/wallet.bridge-select-asset
    :skip-step? (fn [db] (some? (get-in db (conj db-path/send :token))))}
   {:screen-id  :screen/wallet.bridge-to
    :skip-step? (fn [db] (some? (get-in db (conj db-path/send :bridge-to-chain-id))))}
   {:screen-id  :screen/wallet.bridge-input-amount
    :skip-step? (fn [db] (some? (get-in db (conj db-path/send :amount))))}
   {:screen-id :screen/wallet.transaction-confirmation}])
