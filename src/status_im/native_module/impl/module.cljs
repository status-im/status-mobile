(ns status-im.native-module.impl.module
  (:require [status-im.ui.components.react :as r]
            [re-frame.core :as re-frame]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [clojure.string :as string]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]))

(defn status []
  (when (exists? (.-NativeModules rn-dependencies/react-native))
    (.-Status (.-NativeModules rn-dependencies/react-native))))

(defn clear-web-data []
  (when (status)
    (.clearCookies (status))
    (.clearStorageAPIs (status))))

(defn init-keystore []
  (.initKeystore (status)))

(defn open-accounts [callback]
  (.openAccounts (status) #(callback (types/json->clj %))))

(defn prepare-dir-and-update-config [config callback]
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

(defonce listener-initialized (atom false))

(when-not @listener-initialized
  (reset! listener-initialized true)
  (.addListener r/device-event-emitter "gethEvent"
                #(re-frame/dispatch [:signals/signal-received (.-jsonEvent %)])))

(defn node-ready [])

(defonce account-creation? (atom false))

(defn create-account [password on-result]
  (when (status)
    (let [callback (fn [data]
                     (reset! account-creation? false)
                     (on-result data))]
      (swap! account-creation?
             (fn [creation?]
               (if-not creation?
                 (do
                   (.createAccount (status) password callback)
                   true)
                 false))))))

(defn send-data-notification [{:keys [data-payload tokens] :as m} on-result]
  (when (status)
    (.sendDataNotification (status) data-payload tokens on-result)))

(defn send-logs [dbJson js-logs callback]
  (when (status)
    (.sendLogs (status) dbJson js-logs callback)))

(defn add-peer [enode on-result]
  (when (status)
    (.addPeer (status) enode on-result)))

(defn multiaccount-generate-and-derive-addresses [n mnemonic-length paths on-result]
  (.multiAccountGenerateAndDeriveAddresses (status)
                                           (types/clj->json {:n n
                                                             :mnemonicPhraseLength mnemonic-length
                                                             :bip39Passphrase ""
                                                             :paths paths})
                                           on-result))

(defn multiaccount-derive-addresses [account-id paths on-result]
  (when (status)
    (.multiAccountDeriveAddresses (status)
                                  (types/clj->json {:accountID account-id
                                                    :paths paths})
                                  on-result)))

(defn multiaccount-store-account [account-id password on-result]
  (when (status)
    (.multiAccountStoreAccount (status)
                               (types/clj->json {:accountID account-id
                                                 :password password})
                               on-result)))

(defn multiaccount-load-account [address password on-result]
  (when (status)
    (.multiAccountLoadAccount (status)
                              (types/clj->json {:address address
                                                :password password})
                              on-result)))

(defn multiaccount-reset [on-result]
  (when (status)
    (.multiAccountReset (status)
                        on-result)))

(defn multiaccount-store-derived
  [account-id paths password on-result]
  (.multiAccountStoreDerived (status)
                             (types/clj->json {:accountID account-id
                                               :paths paths
                                               :password password})

                             on-result))

(defn multiaccount-import-mnemonic [mnemonic password on-result]
  (when (status)
    (.multiAccountImportMnemonic (status)
                                 (types/clj->json {:mnemonicPhrase  mnemonic
                                                   :Bip39Passphrase password})

                                 on-result)))

(defn verify [address password on-result]
  (.verify (status) address password on-result))

(defn login-with-keycard [whisper-private-key encryption-public-key on-result]
  (clear-web-data)
  (.loginWithKeycard (status) whisper-private-key encryption-public-key on-result))

(defn set-soft-input-mode [mode]
  (when (status)
    (.setSoftInputMode (status) mode)))

(defn call-rpc [payload callback]
  (.callRPC (status) payload callback))

(defn call-private-rpc [payload callback]
  (.callPrivateRPC (status) payload callback))

(defn sign-message [rpcParams callback]
  (.signMessage (status) rpcParams callback))

(defn hash-transaction [rpcParams callback]
  (.hashTransaction (status) rpcParams callback))

(defn hash-message [message callback]
  (.hashMessage (status) message callback))

(defn hash-typed-data [data callback]
  (.hashTypedData (status) data callback))

(defn sign-typed-data [data account password callback]
  (.signTypedData (status) data account password callback))

(defn send-transaction [rpcParams password callback]
  (.sendTransaction (status) rpcParams password callback))

(defn send-transaction-with-signature [rpcParams sig callback]
  (.sendTransactionWithSignature (status) rpcParams sig callback))

(defn close-application []
  (.closeApplication (status)))

(defn connection-change [{:keys [type expensive?]}]
  (.connectionChange (status) type expensive?))

(defn app-state-change [state]
  (.appStateChange (status) state))

(defn get-device-UUID [callback]
  (.getDeviceUUID
   (status)
   (fn [UUID]
     (callback (string/upper-case UUID)))))

(defn set-blank-preview-flag [flag]
  (.setBlankPreviewFlag (status) flag))

(defn extract-group-membership-signatures [signature-pairs callback]
  (when (status)
    (.extractGroupMembershipSignatures (status) signature-pairs callback)))

(defn sign-group-membership [content callback]
  (when (status)
    (.signGroupMembership (status) content callback)))

(defn is24Hour []
  (when (status)
    (.-is24Hour (status))))

(defn get-device-model-info []
  (when status
    {:model     (.-model status)
     :brand     (.-brand status)
     :build-id  (.-buildId status)
     :device-id (.-deviceId status)}))

(defn update-mailservers [enodes on-result]
  (when (status)
    (.updateMailservers (status) enodes on-result)))

(defn chaos-mode-update [on on-result]
  (when (status)
    (.chaosModeUpdate (status) on on-result)))

(defn get-nodes-from-contract [rpc-endpoint contract-address on-result]
  (when (status)
    (.getNodesFromContract (status) rpc-endpoint contract-address on-result)))

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
