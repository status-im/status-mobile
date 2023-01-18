(ns status-im.utils.wallet-connect
  (:require ["@react-native-async-storage/async-storage" :default AsyncStorage]
            ["@walletconnect/client" :refer [CLIENT_EVENTS] :default WalletConnectClient]
            [clojure.string :as string]
            [status-im2.config :as config]))

(defn init
  [on-success on-error]
  (-> ^js WalletConnectClient
      (.init (clj->js {:controller     true
                       :projectId      config/wallet-connect-project-id
                       :logger         "debug"
                       :metadata       config/default-wallet-connect-metadata
                       :storageOptions {:asyncStorage ^js AsyncStorage}}))
      (.then on-success)
      (.catch on-error)))

(defn session-request-event [] (.-request (.-session CLIENT_EVENTS)))

(defn session-created-event [] (.-created (.-session CLIENT_EVENTS)))

(defn session-deleted-event [] (.-deleted (.-session CLIENT_EVENTS)))

(defn session-proposal-event [] (.-proposal (.-session CLIENT_EVENTS)))

(defn session-updated-event [] (.-updated (.-session CLIENT_EVENTS)))

(defn url?
  [url]
  (string/starts-with? url "wc:"))
