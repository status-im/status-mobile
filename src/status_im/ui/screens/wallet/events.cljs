(ns status-im.ui.screens.wallet.events
  (:require [status-im.ui.screens.wallet.signing-phrase.views :as signing-phrase]
            [status-im.ui.starter-pack.events :as sp]
            [status-im.utils.handlers :as handlers]))

(handlers/register-handler-fx
 ::wallet-stack
 (fn [{:keys [db]}]
   (let [wallet-set-up-passed? (get-in db [:multiaccount :wallet-set-up-passed?])
         sign-phrase-showed?   (get db :wallet/sign-phrase-showed?)]
     {:dispatch-n [[::sp/eligible]
                   [::sp/check-amount]
                   [:wallet.ui/pull-to-refresh]] ;TODO temporary simple fix for v1
      :db       (if (or wallet-set-up-passed? sign-phrase-showed?)
                  db
                  (assoc db :popover/popover {:view [signing-phrase/signing-phrase]}
                         :wallet/sign-phrase-showed? true))})))

(handlers/register-handler-fx
 ::wallet-add-custom-token
 (fn [{:keys [db]}]
   {:db (dissoc db :wallet/custom-token-screen)}))
