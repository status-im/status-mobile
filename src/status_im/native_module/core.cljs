(ns status-im.native-module.core
  (:require [status-im.native-module.impl.module :as native-module]))

(def adjust-resize 16)

(defn init-keystore []
  (native-module/init-keystore))

(defn open-accounts [callback]
  (native-module/open-accounts callback))

(defn prepare-dir-and-update-config [config callback]
  (native-module/prepare-dir-and-update-config config callback))

(defn save-account-and-login
  [multiaccount-data password config accounts-data]
  (native-module/save-account-and-login multiaccount-data password config accounts-data))

(defn login
  [account-data password]
  (native-module/login account-data password))

(defn logout
  []
  (native-module/logout))

(defn node-ready [])

(defn create-multiaccount [password callback]
  (native-module/create-account password callback))

(defn multiaccount-load-account [address password callback]
  (native-module/multiaccount-load-account address password callback))

(defn multiaccount-reset [callback]
  (native-module/multiaccount-reset callback))

(defn multiaccount-derive-addresses [account-id paths callback]
  (native-module/multiaccount-derive-addresses account-id paths callback))

(defn multiaccount-store-account [account-id password callback]
  (native-module/multiaccount-store-account account-id password callback))

(defn multiaccount-store-derived [account-id paths password callback]
  (native-module/multiaccount-store-derived account-id paths password callback))

(defn multiaccount-generate-and-derive-addresses [n mnemonic-length paths callback]
  (native-module/multiaccount-generate-and-derive-addresses n mnemonic-length paths callback))

(defn multiaccount-import-mnemonic [mnemonic password callback]
  (native-module/multiaccount-import-mnemonic mnemonic password callback))

(defn verify [address password callback]
  (native-module/verify address password callback))

(defn login-with-keycard
  [{:keys [whisper-private-key encryption-public-key on-result]}]
  (native-module/login-with-keycard whisper-private-key encryption-public-key on-result))

(defn set-soft-input-mode [mode]
  (native-module/set-soft-input-mode mode))

(defn call-rpc [payload callback]
  (native-module/call-rpc payload callback))

(defn call-private-rpc [payload callback]
  (native-module/call-private-rpc payload callback))

(defn sign-message [rpcParams callback]
  (native-module/sign-message rpcParams callback))

(defn sign-typed-data [data account password callback]
  (native-module/sign-typed-data data account password callback))

(defn send-transaction [rpcParams password callback]
  (native-module/send-transaction rpcParams password callback))

(defn hash-transaction [rpcParams callback]
  (native-module/hash-transaction rpcParams callback))

(defn hash-message [message callback]
  (native-module/hash-message message callback))

(defn hash-typed-data [data callback]
  (native-module/hash-typed-data data callback))

(defn send-transaction-with-signature [rpcParams sig callback]
  (native-module/send-transaction-with-signature rpcParams sig callback))

(defn send-data-notification [m callback]
  (native-module/send-data-notification m callback))

(defn send-logs [dbJson js-logs callback]
  (native-module/send-logs dbJson js-logs callback))

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

(defn set-blank-preview-flag [flag]
  (native-module/set-blank-preview-flag flag))

(defn is24Hour []
  (native-module/is24Hour))

(defn get-device-model-info []
  (native-module/get-device-model-info))

(def extract-group-membership-signatures native-module/extract-group-membership-signatures)

(def sign-group-membership native-module/sign-group-membership)

(def update-mailservers native-module/update-mailservers)

(def rooted-device? native-module/rooted-device?)

(def chaos-mode-update native-module/chaos-mode-update)

(def get-nodes-from-contract native-module/get-nodes-from-contract)
