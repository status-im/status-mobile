(ns status-im.utils.wallet-connect-legacy
  (:require ["@walletconnect/client-legacy" :default WalletConnect]
            [taoensso.timbre :as log]
            [status-im.utils.config :as config]))

(defn create-connector [uri]
  (WalletConnect.
   (clj->js {:uri uri
             :clientMeta config/default-wallet-connect-metadata})))

(defn create-connector-from-session [session]
  (log/info "URI" session)
  (WalletConnect.
   (clj->js {:session session
             :clientMeta config/default-wallet-connect-metadata})))
