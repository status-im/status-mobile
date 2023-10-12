(ns status-im2.contexts.wallet.events
  (:require [taoensso.timbre :as log]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-fx :wallet-2/get-wallet-token
 (fn [_ [accounts]]
   (let [params (map :address accounts)]
     {:json-rpc/call [{:method     "wallet_getWalletToken"
                       :params     [params]
                       :on-success [:wallet-2/get-wallet-token-success]
                       :on-error   (fn [error]
                                     (log/info "failed to get wallet token"
                                               {:event  :wallet-2/get-wallet-token
                                                :error  error
                                                :params params}))}]})))

(re-frame/reg-event-fx :wallet-2/get-wallet-token-success
 (fn [{:keys [db]} [data]]
   {:db (assoc db
               :wallet-2/tokens          data
               :wallet-2/tokens-loading? false)}))

