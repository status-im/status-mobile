(ns status-im.native-module.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status_im.utils.db :as utils.db]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(defn status []
  (when (exists? (.-NativeModules rn-dependencies/react-native))
    (.-Status (.-NativeModules rn-dependencies/react-native))))

(def adjust-resize 16)

(defn clear-web-data []
  (log/debug "[native-module] clear-web-data")
  (when (status)
    (.clearCookies (status))
    (.clearStorageAPIs (status))))

(defn init-keystore []
  (log/debug "[native-module] init-keystore")
  (.initKeystore (status)))

(defn open-accounts [callback]
  (log/debug "[native-module] open-accounts")
  (.openAccounts (status) #(callback (types/json->clj %))))

(defn prepare-dir-and-update-config
  [config callback]
  (log/debug "[native-module] prepare-dir-and-update-config")
  (.prepareDirAndUpdateConfig (status)
                              config
                              #(callback (types/json->clj %))))

(defn enable-notifications []
  (log/debug "[native-module] enable-notifications")
  (.enableNotifications (status)))

(defn disable-notifications []
  (log/debug "[native-module] disable-notifications")
  (.disableNotifications (status)))

(defn save-account-and-login
  "NOTE: beware, the password has to be sha3 hashed"
  [multiaccount-data hashed-password config accounts-data]
  (log/debug "[native-module] save-account-and-login")
  (clear-web-data)
  (.saveAccountAndLogin (status) multiaccount-data hashed-password config accounts-data))

(defn save-account-and-login-with-keycard
  "NOTE: chat-key is a whisper private key sent from keycard"
  [multiaccount-data password config chat-key]
  (log/debug "[native-module] save-account-and-login-with-keycard")
  (.saveAccountAndLoginWithKeycard (status) multiaccount-data password config chat-key))

(defn login
  "NOTE: beware, the password has to be sha3 hashed"
  [account-data hashed-password]
  (log/debug "[native-module] login")
  (clear-web-data)
  (.login (status) account-data hashed-password))

(defn logout []
  (log/debug "[native-module] logout")
  (clear-web-data)
  (.logout (status)))

(defonce listener
  (.addListener react/device-event-emitter "gethEvent"
                #(re-frame/dispatch [:signals/signal-received (.-jsonEvent %)])))

(defn multiaccount-load-account
  "NOTE: beware, the password has to be sha3 hashed

   this function is used after storing an account when you still want to
   derive accounts from it, because saving an account flushes the loaded keys
   from memory"
  [address hashed-password callback]
  (log/debug "[native-module] multiaccount-load-account")
  (.multiAccountLoadAccount (status)
                            (types/clj->json {:address address
                                              :password hashed-password})
                            callback))

(defn multiaccount-reset
  "TODO: this function is not used anywhere
   if usage isn't planned, remove"
  [callback]
  (log/debug "[native-module]  multiaccount-reset")
  (.multiAccountReset (status)
                      callback))

(defn multiaccount-derive-addresses
  "NOTE: this should be named derive-accounts
   this only derive addresses, they still need to be stored
   with `multiaccount-store-derived` if you want to be able to
   reuse the derived addresses later"
  [account-id paths callback]
  (log/debug "[native-module]  multiaccount-derive-addresses")
  (when (status)
    (.multiAccountDeriveAddresses (status)
                                  (types/clj->json {:accountID account-id
                                                    :paths paths})
                                  callback)))

(defn multiaccount-store-account
  "NOTE: beware, the password has to be sha3 hashed

   this stores the account and flush keys in memory so
   in order to also store derived accounts like initial wallet
   and chat accounts, you need to load the account again with
   `multiaccount-load-account` before using `multiaccount-store-derived`
   and the id of the account stored will have changed"
  [account-id hashed-password callback]
  (log/debug "[native-module] multiaccount-store-account")
  (when (status)
    (.multiAccountStoreAccount (status)
                               (types/clj->json {:accountID account-id
                                                 :password hashed-password})
                               callback)))

(defn multiaccount-store-derived
  "NOTE: beware, the password has to be sha3 hashed"
  [account-id paths hashed-password callback]
  (log/debug "[native-module]  multiaccount-store-derived")
  (.multiAccountStoreDerived (status)
                             (types/clj->json {:accountID account-id
                                               :paths paths
                                               :password hashed-password})

                             callback))

(defn multiaccount-generate-and-derive-addresses
  "used to generate multiple multiaccounts for onboarding
   NOTE: nothing is saved so you will need to use
   `multiaccount-store-account` on the selected multiaccount
   to store the key"
  [n mnemonic-length paths callback]
  (log/debug "[native-module]  multiaccount-generate-and-derive-addresses")
  (.multiAccountGenerateAndDeriveAddresses (status)
                                           (types/clj->json {:n n
                                                             :mnemonicPhraseLength mnemonic-length
                                                             :bip39Passphrase ""
                                                             :paths paths})
                                           callback))

(defn multiaccount-import-mnemonic
  [mnemonic password callback]
  (log/debug "[native-module] multiaccount-import-mnemonic")
  (.multiAccountImportMnemonic (status)
                               (types/clj->json {:mnemonicPhrase  mnemonic
                                                 ;;NOTE this is not the multiaccount password
                                                 :Bip39Passphrase password})

                               callback))

(defn verify
  "NOTE: beware, the password has to be sha3 hashed"
  [address hashed-password callback]
  (log/debug "[native-module] verify")
  (.verify (status) address hashed-password callback))

(defn login-with-keycard
  [{:keys [multiaccount-data password chat-key]}]
  (log/debug "[native-module] login-with-keycard")
  (clear-web-data)
  (.loginWithKeycard (status) multiaccount-data password chat-key))

(defn set-soft-input-mode [mode]
  (log/debug "[native-module]  set-soft-input-mode")
  (.setSoftInputMode (status) mode))

(defn call-rpc [payload callback]
  (log/debug "[native-module] call-rpc")
  (.callRPC (status) payload callback))

(defn call-private-rpc [payload callback]
  (.callPrivateRPC (status) payload callback))

(defn hash-transaction
  "used for keycard"
  [rpcParams callback]
  (log/debug "[native-module] hash-transaction")
  (.hashTransaction (status) rpcParams callback))

(defn hash-message
  "used for keycard"
  [message callback]
  (log/debug "[native-module] hash-message")
  (.hashMessage (status) message callback))

(defn hash-typed-data
  "used for keycard"
  [data callback]
  (log/debug "[native-module] hash-typed-data")
  (.hashTypedData (status) data callback))

(defn send-transaction-with-signature
  "used for keycard"
  [rpcParams sig callback]
  (log/debug "[native-module] send-transaction-with-signature")
  (.sendTransactionWithSignature (status) rpcParams sig callback))

(defn sign-message
  "NOTE: beware, the password in rpcParams has to be sha3 hashed"
  [rpcParams callback]
  (log/debug "[native-module] sign-message")
  (.signMessage (status) rpcParams callback))

(defn send-transaction
  "NOTE: beware, the password has to be sha3 hashed"
  [rpcParams hashed-password callback]
  (log/debug "[native-module] send-transaction")
  (.sendTransaction (status) rpcParams hashed-password callback))

(defn sign-typed-data
  "NOTE: beware, the password has to be sha3 hashed"
  [data account hashed-password callback]
  (log/debug "[native-module] clear-web-data")
  (.signTypedData (status) data account hashed-password callback))

(defn send-logs [dbJson js-logs callback]
  (log/debug "[native-module] send-logs")
  (.sendLogs (status) dbJson js-logs callback))

(defn add-peer [enode on-result]
  (log/debug "[native-module] add-peer")
  (.addPeer (status) enode on-result))

(defn close-application []
  (log/debug "[native-module] close-application")
  (.closeApplication (status)))

(defn connection-change [type expensive?]
  (log/debug "[native-module] connection-change")
  (.connectionChange (status) type (boolean expensive?)))

(defn app-state-change [state]
  (log/debug "[native-module] app-state-change")
  (.appStateChange (status) state))

(defn set-blank-preview-flag [flag]
  (log/debug "[native-module] set-blank-preview-flag")
  (.setBlankPreviewFlag (status) flag))

(defn is24Hour []
  (log/debug "[native-module] is24Hour")
  ;;NOTE: we have to check for status module because of tests
  (when (status)
    (.-is24Hour (status))))

(defn get-device-model-info []
  (log/debug "[native-module] get-device-model-info")
  ;;NOTE: we have to check for status module because of tests
  (when (status)
    {:model     (.-model (status))
     :brand     (.-brand (status))
     :build-id  (.-buildId (status))
     :device-id (.-deviceId (status))}))

(defn extract-group-membership-signatures
  [signature-pairs callback]
  (log/debug "[native-module] extract-group-membership-signatures")
  (.extractGroupMembershipSignatures (status) signature-pairs callback))

(defn sign-group-membership [content callback]
  (log/debug "[native-module] sign-group-membership")
  (.signGroupMembership (status) content callback))

(defn update-mailservers
  [enodes on-result]
  (log/debug "[native-module] update-mailservers")
  (.updateMailservers (status) enodes on-result))

(defn chaos-mode-update [on on-result]
  (log/debug "[native-module] chaos-mode-update")
  (.chaosModeUpdate (status) on on-result))

(defn get-nodes-from-contract
  [rpc-endpoint contract-address on-result]
  (log/debug "[native-module] get-nodes-from-contract")
  (.getNodesFromContract (status) rpc-endpoint contract-address on-result))

(defn rooted-device? [callback]
  (log/debug "[native-module] rooted-device?")
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

(defn generate-gfycat
  "Generate a 3 words random name based on the user public-key, synchronously"
  [public-key]
  {:pre [(utils.db/valid-public-key? public-key)]}
  (log/debug "[native-module] generate-gfycat")
  (.generateAlias (status) public-key))

(defn identicon
  "Generate a icon based on a string, synchronously"
  [seed]
  (log/debug "[native-module] identicon")
  (.identicon (status) seed))
