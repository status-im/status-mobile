(ns status-im.utils.wallet-connect-legacy
  (:require ["@walletconnect/client-legacy" :default WalletConnect]
            [status-im2.config :as config]))

(defn create-connector
  [uri]
  (WalletConnect.
   (clj->js {:uri        uri
             :clientMeta config/default-wallet-connect-metadata})))

(defn create-connector-from-session
  [session]
  (WalletConnect.
   (clj->js {:session    session
             :clientMeta config/default-wallet-connect-metadata})))
