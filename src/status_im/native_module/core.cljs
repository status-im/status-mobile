(ns status-im.native-module.core
  (:require [status-im.ui.components.react :as r]
            [re-frame.core :as re-frame]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [clojure.string :as string]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]))

(defn status []
  (when (exists? (.-NativeModules rn-dependencies/react-native))
    (.-Status (.-NativeModules rn-dependencies/react-native))))

(def adjust-resize 16)

(defn clear-web-data []
  (when (status)
    (.clearCookies (status))
    (.clearStorageAPIs (status))))

(defn init-keystore []
  (.initKeystore (status)))

(defn open-accounts [callback]
  (.openAccounts (status) #(callback (types/json->clj %))))

(defn prepare-dir-and-update-config
  [config callback]
  (.prepareDirAndUpdateConfig (status)
                              config
                              #(callback (types/json->clj %))))

(defn save-account-and-login
  [multiaccount-data password config accounts-data]
  (clear-web-data)
  (.saveAccountAndLogin (status) multiaccount-data password config accounts-data))

(defn login
  [account-data password]
  (clear-web-data)
  (.login (status) account-data password))

(defn logout []
  (clear-web-data)
  (.logout (status)))

(defonce listener
  (.addListener r/device-event-emitter "gethEvent"
                #(re-frame/dispatch [:signals/signal-received (.-jsonEvent %)])))

(defn multiaccount-load-account
  "this function is used after storing an account when you still want to
   derive accounts from it, because saving an account flushes the loaded keys
   from memory"
  [address password callback]
  (.multiAccountLoadAccount (status)
                            (types/clj->json {:address address
                                              :password password})
                            callback))

(defn multiaccount-reset
  "TODO: this function is not used anywhere
   if usage isn't planned, remove"
  [callback]
  (.multiAccountReset (status)
                      callback))

(defn multiaccount-derive-addresses
  "NOTE: this should be named derive-accounts
   this only derive addresses, they still need to be stored
   with `multiaccount-store-derived` if you want to be able to
   reuse the derived addresses later"
  [account-id paths callback]
  (when (status)
    (.multiAccountDeriveAddresses (status)
                                  (types/clj->json {:accountID account-id
                                                    :paths paths})
                                  callback)))

(defn multiaccount-store-account
  "this stores the account and flush keys in memory so
   in order to also store derived accounts like initial wallet
   and chat accounts, you need to load the account again with
   `multiaccount-load-account` before using `multiaccount-store-derived`
   and the id of the account stored will have changed"
  [account-id password callback]
  (when (status)
    (.multiAccountStoreAccount (status)
                               (types/clj->json {:accountID account-id
                                                 :password password})
                               callback)))

(defn multiaccount-store-derived
  [account-id paths password callback]
  (.multiAccountStoreDerived (status)
                             (types/clj->json {:accountID account-id
                                               :paths paths
                                               :password password})

                             callback))

(defn multiaccount-generate-and-derive-addresses
  "used to generate multiple multiaccounts for onboarding
   NOTE: nothing is saved so you will need to use
   `multiaccount-store-account` on the selected multiaccount
   to store the key"
  [n mnemonic-length paths callback]
  (.multiAccountGenerateAndDeriveAddresses (status)
                                           (types/clj->json {:n n
                                                             :mnemonicPhraseLength mnemonic-length
                                                             :bip39Passphrase ""
                                                             :paths paths})
                                           callback))

(defn multiaccount-import-mnemonic
  [mnemonic password callback]
  (.multiAccountImportMnemonic (status)
                               (types/clj->json {:mnemonicPhrase  mnemonic
                                                 :Bip39Passphrase password})

                               callback))

(defn verify [address password callback]
  (.verify (status) address password callback))

(defn login-with-keycard
  [{:keys [whisper-private-key encryption-public-key on-result]}]
  (clear-web-data)
  (.loginWithKeycard (status) whisper-private-key encryption-public-key on-result))

(defn set-soft-input-mode [mode]
  (.setSoftInputMode (status) mode))

(defn call-rpc [payload callback]
  (.callRPC (status) payload callback))

(defn call-private-rpc [payload callback]
  (.callPrivateRPC (status) payload callback))

(defn hash-transaction [rpcParams callback]
  (.hashTransaction (status) rpcParams callback))

(defn hash-message [message callback]
  (.hashMessage (status) message callback))

(defn hash-typed-data [data callback]
  (.hashTypedData (status) data callback))

(defn sign-message [rpcParams callback]
  (.signMessage (status) rpcParams callback))

(defn sign-typed-data [data account password callback]
  (.signTypedData (status) data account password callback))

(defn send-transaction [rpcParams password callback]
  (.sendTransaction (status) rpcParams password callback))

(defn send-transaction-with-signature [rpcParams sig callback]
  (.sendTransactionWithSignature (status) rpcParams sig callback))

(defn send-data-notification
  [{:keys [data-payload tokens] :as m} on-result]
  (.sendDataNotification (status) data-payload tokens on-result))

(defn send-logs [dbJson js-logs callback]
  (.sendLogs (status) dbJson js-logs callback))

(defn add-peer [enode on-result]
  (.addPeer (status) enode on-result))

(defn close-application []
  (.closeApplication (status)))

(defn connection-change [type expensive?]
  (.connectionChange (status) type (boolean expensive?)))

(defn app-state-change [state]
  (.appStateChange (status) state))

(defn get-device-UUID [callback]
  (.getDeviceUUID
   (status)
   (fn [UUID]
     (callback (string/upper-case UUID)))))

(defn set-blank-preview-flag [flag]
  (.setBlankPreviewFlag (status) flag))

(defn is24Hour []
  ;;NOTE: we have to check for status module because of tests
  (when (status)
    (.-is24Hour (status))))

(defn get-device-model-info []
  ;;NOTE: we have to check for status module because of tests
  (when (status)
    {:model     (.-model (status))
     :brand     (.-brand (status))
     :build-id  (.-buildId (status))
     :device-id (.-deviceId (status))}))

(defn extract-group-membership-signatures
  [signature-pairs callback]
  (.extractGroupMembershipSignatures (status) signature-pairs callback))

(defn sign-group-membership [content callback]
  (.signGroupMembership (status) content callback))

(defn update-mailservers
  [enodes on-result]
  (.updateMailservers (status) enodes on-result))

(defn chaos-mode-update [on on-result]
  (.chaosModeUpdate (status) on on-result))

(defn get-nodes-from-contract
  [rpc-endpoint contract-address on-result]
  (.getNodesFromContract (status) rpc-endpoint contract-address on-result))

(defn rooted-device? [callback]
  (cond
    ;; we assume that iOS is safe by default
    platform/ios?
    (callback false)

    ;; we assume that Desktop is unsafe by default
    ;; (theoretically, Desktop is always "rooted", by design
    platform/desktop?
    (callback true)

    ;; we check root on android
    platform/android?
    (if (status)
      (.isDeviceRooted (status) callback)
      ;; if module isn't initialized we return true to avoid degrading security
      (callback true))

    ;; in unknown scenarios we also consider the device rooted to avoid degrading security
    :else (callback true)))
