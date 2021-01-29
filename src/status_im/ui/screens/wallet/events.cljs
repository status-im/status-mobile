(ns status-im.ui.screens.wallet.events
  (:require [status-im.ui.screens.wallet.signing-phrase.views :as signing-phrase]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.fx :as fx]))

(fx/defn wallet-will-focus
  {:events [::wallet-stack]}
  [{:keys [db] :as cofx}]
  (let [wallet-set-up-passed? (get-in db [:multiaccount :wallet-set-up-passed?])
        sign-phrase-showed? (get db :wallet/sign-phrase-showed?)]
    {:dispatch [:wallet.ui/pull-to-refresh] ;TODO temporary simple fix for v1
     :db       (if (or wallet-set-up-passed? sign-phrase-showed?)
                 db
                 (assoc db :popover/popover {:view [signing-phrase/signing-phrase]}
                        :wallet/sign-phrase-showed? true))}))

(handlers/register-handler-fx
 ::wallet-add-custom-token
 (fn [{:keys [db]}]
   {:db (dissoc db :wallet/custom-token-screen)}))
