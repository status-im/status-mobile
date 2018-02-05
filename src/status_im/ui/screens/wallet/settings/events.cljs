(ns status-im.ui.screens.wallet.settings.events
  (:require [status-im.ui.screens.accounts.events :as accounts]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.handlers :as handlers]))

(defn- toggle-checked [ids id checked?]
  (if checked?
    (conj (or ids #{}) id)
    (disj ids id)))

(handlers/register-handler-fx
  :wallet.settings/toggle-visible-token
  (fn [{{:keys [network] :as db} :db} [_ symbol checked?]]
    (let [chain        (ethereum/network->chain-keyword network)
          path         [:accounts/account :settings]
          settings     (get-in db path)
          new-settings (update-in settings [:wallet :visible-tokens chain] #(toggle-checked % symbol checked?))]
      (-> db
          (assoc-in path new-settings)
          (accounts/update-wallet-settings new-settings)))))
