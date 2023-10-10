(ns status-im2.contexts.wallet.events
  (:require [utils.re-frame :as rf]
            [taoensso.timbre :as log]))

(rf/defn get-wallet-token
  {:events [:wallet-2/get-wallet-token]}
  [_ accounts]
  {:json-rpc/call [{:method     "wallet_getWalletToken"
                    :params     [(map :address accounts)]
                    :on-success #(rf/dispatch [:wallet-2/get-wallet-token-success %])
                    :on-error   #(log/info "failed " %)}]})

(rf/defn get-wallet-token-success
  {:events [:wallet-2/get-wallet-token-success]}
  [{:keys [db]} data]
  {:db (assoc db
              :wallet-2/tokens          data
              :wallet-2/tokens-loading? false)})

