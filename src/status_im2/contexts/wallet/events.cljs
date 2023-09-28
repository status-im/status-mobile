(ns status-im2.contexts.wallet.events 
  (:require [utils.re-frame :as rf]
            [taoensso.timbre :as log]))

(rf/defn get-wallet-tokens
  {:events [:wallet-2/get-wallet-tokens]}
  [_ accounts]
  {:json-rpc/call [{:method      "wallet_getWalletToken"
                    :params      [(map :address accounts)]
                    :on-success  #(rf/dispatch [:wallet-2/get-wallet-tokens-success %])
                    :on-error    #(log/info "failed " %)}]})

(rf/defn get-wallet-tokens-success
  {:events [:wallet-2/get-wallet-tokens-success]}
  [{:keys [db]} data]
  {:db (assoc db 
              :wallet-2/tokens data 
              :wallet-2/tokens-loading? false)})

