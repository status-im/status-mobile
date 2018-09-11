(ns status-im.ui.screens.wallet.settings.models
  (:require [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.ui.screens.wallet.events :as wallet.events]
            [status-im.utils.ethereum.tokens :as tokens]
            [re-frame.core :as re-frame]))

(defn- set-checked [ids id checked?]
  (if checked?
    (conj (or ids #{}) id)
    (disj ids id)))

(defn toggle-visible-token [symbol checked? update-settings-fx {{:keys [account/account]} :db :as cofx}]
  (let [network      (get (:networks account) (:network account))
        chain        (ethereum/network->chain-keyword network)
        settings     (get account :settings)
        new-settings (update-in settings [:wallet :visible-tokens chain] #(set-checked % symbol checked?))]
    (update-settings-fx new-settings cofx)))

(defn configure-token-balance-and-visibility [symbol balance update-settings-fx cofx]
  (handlers-macro/merge-fx cofx
                           (toggle-visible-token symbol true update-settings-fx)
                           ;;TODO(goranjovic): move `update-token-balance-success` function to wallet models
                           (wallet.events/update-token-balance-success symbol balance)))

(defn wallet-autoconfig-tokens [{:keys [db]}]
  (let [{:keys [account/account web3 network-status]} db
        network   (get (:networks account) (:network account))
        chain     (ethereum/network->chain-keyword network)
        contracts (->> (tokens/tokens-for chain)
                       (remove :hidden?))]
    (when-not (= network-status :offline)
      (doseq [{:keys [address symbol]} contracts]
        ;;TODO(goranjovic): move `get-token-balance` function to wallet models
        (wallet.events/get-token-balance {:web3       web3
                                          :contract   address
                                          :account-id (:address account)
                                          :on-error   #(re-frame/dispatch [:update-token-balance-fail symbol %])
                                          :on-success #(when (> % 0)
                                                         (re-frame/dispatch [:configure-token-balance-and-visibility symbol %]))})))))
