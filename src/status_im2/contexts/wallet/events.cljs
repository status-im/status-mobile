(ns status-im2.contexts.wallet.events
  (:require [utils.re-frame :as rf]
            [taoensso.timbre :as log]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-fx :wallet-2/get-wallet-token
 (fn [_ [accounts]]
   {:json-rpc/call [{:method     "wallet_getWalletToken"
                     :params     [(map :address accounts)]
                     :on-success #(rf/dispatch [:wallet-2/get-wallet-token-success %])
                     :on-error   #(log/info "failed " %)}]}))

(re-frame/reg-event-fx :wallet-2/get-wallet-token-success
  (fn [{:keys [db]} [data]]
    {:db (assoc db
                :wallet-2/tokens          data
                :wallet-2/tokens-loading? false)}))

