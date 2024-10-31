(ns status-im.contexts.wallet.wallet-connect.utils.rpc
  (:require [promesa.core :as promesa]
            [status-im.common.json-rpc.events :as rpc-events]
            [utils.transforms :as transforms]))

(defn wallet-disconnect-persisted-session
  [topic]
  (rpc-events/call-async "wallet_disconnectWalletConnectSession" true topic))

(defn wallet-get-persisted-sessions
  ([]
   (let [now (-> (js/Date.) .getTime (quot 1000))]
     (wallet-get-persisted-sessions now)))
  ([expiry-timestamp]
   (rpc-events/call-async "wallet_getWalletConnectActiveSessions" false expiry-timestamp)))

(defn wallet-persist-session
  [session]
  (->> session
       transforms/clj->json
       (rpc-events/call-async "wallet_addWalletConnectSession" false)))

(defn wallet-get-transaction-estimated-time
  [chain-id max-fee-per-gas]
  (-> (rpc-events/call-async "wallet_getTransactionEstimatedTime" true chain-id max-fee-per-gas)
      (promesa/then transforms/js->clj)))
