(ns react-native.status
  (:require ["react-native" :as react-native]
            [taoensso.timbre :as log]
            [utils.types :as types]))

(defn status
  []
  (when (exists? (.-NativeModules react-native))
    (.-Status ^js (.-NativeModules react-native))))

(def adjust-resize 16)

(defn open-accounts
  [callback]
  (log/debug "[native-module] open-accounts")
  (.openAccounts ^js (status) #(callback (types/json->clj %))))

(defn set-soft-input-mode
  [mode]
  (log/debug "[native-module]  set-soft-input-mode")
  (.setSoftInputMode ^js (status) mode))

(defn call-private-rpc
  [payload callback]
  (.callPrivateRPC ^js (status) payload callback))

(defn get-connection-string-for-bootstrapping-another-device
  "Generates connection string form status-go for the purpose of local pairing on the sender end"
  [config-json callback]
  (log/info "[native-module] Fetching Connection String"
            {:fn          :get-connection-string-for-bootstrapping-another-device
             :config-json config-json})
  (.getConnectionStringForBootstrappingAnotherDevice ^js (status) config-json callback))

(defn input-connection-string-for-bootstrapping
  "Provides connection string to status-go for the purpose of local pairing on the receiver end"
  [connection-string config-json callback]
  (log/info "[native-module] Sending Connection String"
            {:fn                :input-connection-string-for-bootstrapping
             :config-json       config-json
             :connection-string connection-string})
  (.inputConnectionStringForBootstrapping ^js (status) connection-string config-json callback))

(defn sha3
  [str]
  (log/debug "[native-module] sha3")
  (.sha3 ^js (status) str))
