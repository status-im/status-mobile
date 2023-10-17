(ns status-im2.contexts.wallet.events
  (:require [utils.re-frame :as rf]))

(rf/defn scan-address-success
  {:events [:wallet-2/scan-address-success]}
  [{:keys [db]} address]
  {:db (assoc db :wallet-2/scanned-address address)})

(rf/defn clean-scanned-address
  {:events [:wallet-2/clean-scanned-address]}
  [{:keys [db]}]
  {:db (dissoc db :wallet-2/scanned-address)})
