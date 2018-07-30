(ns status-im.native-module.core
  (:require [status-im.native-module.impl.module :as native-module]))

(def adjust-resize 16)

(defn move-to-internal-storage [callback]
  (native-module/move-to-internal-storage callback))

(defn start-node [config]
  (native-module/start-node config))

(defn stop-node []
  (native-module/stop-node))

(defn create-account [password callback]
  (native-module/create-account password callback))

(defn recover-account [passphrase password callback]
  (native-module/recover-account passphrase password callback))

(defn login [address password callback]
  (native-module/login address password callback))

(defn approve-sign-request [id password callback]
  (native-module/approve-sign-request id password callback))

(defn approve-sign-request-with-args [id password gas gas-price callback]
  (native-module/approve-sign-request-with-args id password gas gas-price callback))

(defn discard-sign-request [id]
  (native-module/discard-sign-request id))

(defn set-soft-input-mode [mode]
  (native-module/set-soft-input-mode mode))

(defn clear-web-data []
  (native-module/clear-web-data))

(defn call-web3 [payload callback]
  (native-module/call-web3 payload callback))

(defn call-web3-private [payload callback]
  (native-module/call-web3-private payload callback))

(defn module-initialized! []
  (native-module/module-initialized!))

(defn should-move-to-internal-storage? [callback]
  (native-module/should-move-to-internal-storage? callback))

(defn notify-users [m callback]
  (native-module/notify-users m callback))

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
