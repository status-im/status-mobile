(ns status-im.ui.screens.wallet.events
  (:require [status-im.ui.screens.wallet.signing-phrase.views :as signing-phrase]
            [status-im.utils.handlers :as handlers]
            [status-im.async-storage.core :as async-storage]
            [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]))

(fx/defn get-buy-crypto-preference
  {:events [::get-buy-crypto]}
  [_]
  {::async-storage/get {:keys [:buy-crypto-hidden]
                        :cb   #(re-frame/dispatch [::store-buy-crypto-preference %])}})

(fx/defn wallet-will-focus
  {:events [::wallet-stack]}
  [{:keys [db] :as cofx}]
  (let [wallet-set-up-passed? (get-in db [:multiaccount :wallet-set-up-passed?])
        sign-phrase-showed? (get db :wallet/sign-phrase-showed?)]
    (fx/merge cofx
              {:dispatch [:wallet.ui/pull-to-refresh] ;TODO temporary simple fix for v1
               :db       (if (or wallet-set-up-passed? sign-phrase-showed?)
                           db
                           (assoc db :popover/popover {:view [signing-phrase/signing-phrase]}
                                  :wallet/sign-phrase-showed? true))}
              (get-buy-crypto-preference))))

(handlers/register-handler-fx
 ::wallet-add-custom-token
 (fn [{:keys [db]}]
   {:db (dissoc db :wallet/custom-token-screen)}))

(fx/defn hide-buy-crypto
  {:events [::hide-buy-crypto]}
  [{:keys [db]}]
  {:db                  (assoc db :wallet/buy-crypto-hidden true)
   ::async-storage/set! {:buy-crypto-hidden true}})

(fx/defn store-buy-crypto
  {:events [::store-buy-crypto-preference]}
  [{:keys [db]} {:keys [buy-crypto-hidden]}]
  {:db (assoc db :wallet/buy-crypto-hidden buy-crypto-hidden)})
