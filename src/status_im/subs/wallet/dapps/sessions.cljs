(ns status-im.subs.wallet.dapps.sessions
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im.contexts.wallet.wallet-connect.utils.networks :as networks]
            [utils.string]))

(rf/reg-sub
 :wallet-connect/sessions-for-current-account
 :<- [:wallet-connect/sessions]
 :<- [:wallet/current-viewing-account-address]
 (fn [[sessions address]]
   (filter
    (fn [{:keys [accounts]}]
      (some #(string/includes? % address) accounts))
    sessions)))

(rf/reg-sub
 :wallet-connect/sessions-for-current-account-and-networks
 ;; NOTE: using all chain-ids (as opposed to only the active ones) for showing sessions,
 ;; so that we don't end up hiding sessions if the session network(s) are deactivated
 :<- [:wallet/chain-ids]
 :<- [:wallet-connect/sessions-for-current-account]
 (fn [[chain-ids sessions]]
   (filter
    (partial networks/session-networks-allowed? chain-ids)
    sessions)))

