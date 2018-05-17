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
 (fn [{{:keys [account/account] :as db} :db :as cofx} [_ symbol checked?]]
   (let [network      (get (:networks account) (:network account))
         chain        (ethereum/network->chain-keyword network)
         settings     (get account :settings)
         new-settings (update-in settings [:wallet :visible-tokens chain] #(toggle-checked % symbol checked?))]
     (accounts/update-settings new-settings cofx))))
