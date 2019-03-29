(ns status-im.native-module.core
  (:require [status-im.native-module.impl.module :as native-module]))

(def adjust-resize 16)

(defn start-node [config]
  (native-module/start-node config))

(def node-started (atom false))

(defn node-ready []
  (reset! node-started true)
  (native-module/node-ready))

(defn stop-node []
  (reset! node-started false)
  (native-module/stop-node))

(defn create-account [password callback]
  (native-module/create-account password callback))

(defn recover-account [passphrase password callback]
  (native-module/recover-account passphrase password callback))

(defn login [address password callback]
  (native-module/login address password callback))

(defn verify [address password callback]
  (native-module/verify address password callback))

(defn login-with-keycard
  [{:keys [whisper-private-key encryption-public-key on-result]}]
  (native-module/login-with-keycard whisper-private-key encryption-public-key on-result))

(defn set-soft-input-mode [mode]
  (native-module/set-soft-input-mode mode))

(defn clear-web-data []
  (native-module/clear-web-data))

(defn call-rpc [payload callback]
  (when @node-started
    (native-module/call-rpc payload callback)))

(defn call-private-rpc [payload callback]
  (when @node-started
    (native-module/call-private-rpc payload callback)))

(defn sign-message [rpcParams callback]
  (native-module/sign-message rpcParams callback))

(defn sign-typed-data [data password callback]
  (native-module/sign-typed-data data password callback))

(defn send-transaction [rpcParams password callback]
  (native-module/send-transaction rpcParams password callback))

(defn hash-transaction [rpcParams callback]
  (native-module/hash-transaction rpcParams callback))

(defn send-transaction-with-signature [rpcParams sig callback]
  (native-module/send-transaction-with-signature rpcParams sig callback))

(defn send-data-notification [m callback]
  (native-module/send-data-notification m callback))

(defn send-logs [dbJson]
  (native-module/send-logs dbJson))

(defn add-peer [enode callback]
  (native-module/add-peer enode callback))

(defn close-application []
  (native-module/close-application))

(defn connection-change [data]
  (native-module/connection-change data))

(defn app-state-change [state]
  (native-module/app-state-change state))

(defn get-device-UUID [callback]
  (native-module/get-device-UUID callback))

(defn is24Hour []
  (native-module/is24Hour))

(def extract-group-membership-signatures native-module/extract-group-membership-signatures)

(def sign-group-membership native-module/sign-group-membership)

(def enable-installation native-module/enable-installation)

(def disable-installation native-module/disable-installation)

(def update-mailservers native-module/update-mailservers)

(def rooted-device? native-module/rooted-device?)

(def chaos-mode-update native-module/chaos-mode-update)

(def get-nodes-from-contract native-module/get-nodes-from-contract)
