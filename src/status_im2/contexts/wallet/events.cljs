(ns status-im2.contexts.wallet.events
  (:require [taoensso.timbre :as log]
            [utils.re-frame :as rf]))

(rf/defn get-wallet-token
  {:events [:wallet-2/get-wallet-token]}
  [_ accounts]
  (let [params (map :address accounts)]
    {:json-rpc/call [{:method     "wallet_getWalletToken"
                      :params     [params]
                      :on-success #(rf/dispatch [:wallet-2/get-wallet-token-success %])
                      :on-error   (fn [error]
                                    (log/info "failed to get wallet token"
                                              {:event  :wallet-2/get-wallet-token
                                               :error  error
                                               :params params}))}]}))

(rf/defn get-wallet-token-success
  {:events [:wallet-2/get-wallet-token-success]}
  [{:keys [db]} data]
  {:db (assoc db
              :wallet-2/tokens          data
              :wallet-2/tokens-loading? false)})
