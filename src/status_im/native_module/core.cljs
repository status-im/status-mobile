(ns status-im.native-module.core
  (:require [re-frame.core :as re-frame]
            [status-im.utils.db :as utils.db]
            [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            ["react-native" :as react-native]))

(defn status []
  (when (exists? (.-NativeModules react-native))
    (.-Status ^js (.-NativeModules react-native))))

(def adjust-resize 16)

(defn clear-web-data []
  (log/debug "[native-module] clear-web-data")
  (when (status)
    (.clearCookies ^js (status))
    (.clearStorageAPIs ^js (status))))

(defn init-keystore [key-uid callback]
  (log/debug "[native-module] init-keystore" key-uid)
  (.initKeystore ^js (status) key-uid callback))

(defn open-accounts [callback]
  (log/debug "[native-module] open-accounts")
  (.openAccounts ^js (status) #(callback (types/json->clj %))))

(defn prepare-dir-and-update-config
  [key-uid config callback]
  (log/debug "[native-module] prepare-dir-and-update-config")
  (.prepareDirAndUpdateConfig ^js (status)
                              key-uid
                              config
                              #(callback (types/json->clj %))))

(defn enable-notifications []
  (log/debug "[native-module] enable-notifications")
  (.enableNotifications ^js (status)))

(defn disable-notifications []
  (log/debug "[native-module] disable-notifications")
  (.disableNotifications ^js (status)))

(defn save-account-and-login
  "NOTE: beware, the password has to be sha3 hashed"
  [key-uid multiaccount-data hashed-password settings config accounts-data]
  (log/debug "[native-module] save-account-and-login"
             "multiaccount-data" multiaccount-data)
  (clear-web-data)
  (init-keystore
   key-uid
   #(.saveAccountAndLogin
     ^js (status) multiaccount-data hashed-password settings config accounts-data)))

(defn save-multiaccount-and-login-with-keycard
  "NOTE: chat-key is a whisper private key sent from keycard"
  [key-uid multiaccount-data password settings config accounts-data chat-key]
  (log/debug "[native-module] save-account-and-login-with-keycard")
  (init-keystore
   key-uid
   #(.saveAccountAndLoginWithKeycard
     ^js (status) multiaccount-data password settings config accounts-data chat-key)))

(defn login
  "NOTE: beware, the password has to be sha3 hashed"
  [key-uid account-data hashed-password]
  (log/debug "[native-module] login")
  (clear-web-data)
  (init-keystore
   key-uid
   #(.login ^js (status) account-data hashed-password)))

(defn logout []
  (log/debug "[native-module] logout")
  (clear-web-data)
  (.logout ^js (status)))

(defonce listener
  (.addListener ^js react/device-event-emitter "gethEvent"
                #(re-frame/dispatch [:signals/signal-received (.-jsonEvent ^js %)])))

(defn multiaccount-load-account
  "NOTE: beware, the password has to be sha3 hashed

   this function is used after storing an account when you still want to
   derive accounts from it, because saving an account flushes the loaded keys
   from memory"
  [address hashed-password callback]
  (log/debug "[native-module] multiaccount-load-account")
  (.multiAccountLoadAccount ^js (status)
                            (types/clj->json {:address address
                                              :password hashed-password})
                            callback))

(defn multiaccount-reset
  "TODO: this function is not used anywhere
   if usage isn't planned, remove"
  [callback]
  (log/debug "[native-module]  multiaccount-reset")
  (.multiAccountReset ^js (status)
                      callback))

(defn multiaccount-derive-addresses
  "NOTE: this should be named derive-accounts
   this only derive addresses, they still need to be stored
   with `multiaccount-store-derived` if you want to be able to
   reuse the derived addresses later"
  [account-id paths callback]
  (log/debug "[native-module]  multiaccount-derive-addresses")
  (when (status)
    (.multiAccountDeriveAddresses ^js (status)
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
  [account-id key-uid hashed-password callback]
  (log/debug "[native-module] multiaccount-store-account")
  (when (status)
    (init-keystore
     key-uid
     #(.multiAccountStoreAccount  ^js (status)
                                  (types/clj->json {:accountID account-id
                                                    :password  hashed-password})
                                  callback))))

(defn multiaccount-store-derived
  "NOTE: beware, the password has to be sha3 hashed"
  [account-id key-uid paths hashed-password callback]
  (log/debug "[native-module] multiaccount-store-derived"
             "account-id" account-id)
  (init-keystore
   key-uid
   #(.multiAccountStoreDerived  ^js (status)
                                (types/clj->json {:accountID account-id
                                                  :paths     paths
                                                  :password  hashed-password})
                                callback)))

(defn multiaccount-generate-and-derive-addresses
  "used to generate multiple multiaccounts for onboarding
   NOTE: nothing is saved so you will need to use
   `multiaccount-store-account` on the selected multiaccount
   to store the key"
  [n mnemonic-length paths callback]
  (log/debug "[native-module]  multiaccount-generate-and-derive-addresses")
  (.multiAccountGenerateAndDeriveAddresses  ^js (status)
                                            (types/clj->json {:n n
                                                              :mnemonicPhraseLength mnemonic-length
                                                              :bip39Passphrase ""
                                                              :paths paths})
                                            callback))

(defn multiaccount-import-mnemonic
  [mnemonic password callback]
  (log/debug "[native-module] multiaccount-import-mnemonic")
  (.multiAccountImportMnemonic  ^js (status)
                                (types/clj->json {:mnemonicPhrase  mnemonic
                                                  ;;NOTE this is not the multiaccount password
                                                  :Bip39Passphrase password})
                                callback))

(defn multiaccount-import-private-key
  [private-key callback]
  (log/debug "[native-module] multiaccount-import-private-key")
  (.multiAccountImportPrivateKey ^js (status)
                                 (types/clj->json {:privateKey  private-key})
                                 callback))

(defn verify
  "NOTE: beware, the password has to be sha3 hashed"
  [address hashed-password callback]
  (log/debug "[native-module] verify")
  (.verify ^js (status) address hashed-password callback))

(defn login-with-keycard
  [{:keys [key-uid multiaccount-data password chat-key]}]
  (log/debug "[native-module] login-with-keycard")
  (clear-web-data)
  (init-keystore
   key-uid
   #(.loginWithKeycard ^js (status) multiaccount-data password chat-key)))

(defn set-soft-input-mode [mode]
  (log/debug "[native-module]  set-soft-input-mode")
  (.setSoftInputMode ^js (status) mode))

(defn call-rpc [payload callback]
  (log/debug "[native-module] call-rpc")
  (.callRPC ^js (status) payload callback))

(defn call-private-rpc [payload callback]
  (.callPrivateRPC ^js (status) payload callback))

(defn hash-transaction
  "used for keycard"
  [rpcParams callback]
  (log/debug "[native-module] hash-transaction")
  (.hashTransaction ^js (status) rpcParams callback))

(defn hash-message
  "used for keycard"
  [message callback]
  (log/debug "[native-module] hash-message")
  (.hashMessage ^js (status) message callback))

(defn hash-typed-data
  "used for keycard"
  [data callback]
  (log/debug "[native-module] hash-typed-data")
  (.hashTypedData ^js (status) data callback))

(defn send-transaction-with-signature
  "used for keycard"
  [rpcParams sig callback]
  (log/debug "[native-module] send-transaction-with-signature")
  (.sendTransactionWithSignature ^js (status) rpcParams sig callback))

(defn sign-message
  "NOTE: beware, the password in rpcParams has to be sha3 hashed"
  [rpcParams callback]
  (log/debug "[native-module] sign-message")
  (.signMessage ^js (status) rpcParams callback))

(defn send-transaction
  "NOTE: beware, the password has to be sha3 hashed"
  [rpcParams hashed-password callback]
  (log/debug "[native-module] send-transaction")
  (.sendTransaction ^js (status) rpcParams hashed-password callback))

(defn sign-typed-data
  "NOTE: beware, the password has to be sha3 hashed"
  [data account hashed-password callback]
  (log/debug "[native-module] clear-web-data")
  (.signTypedData ^js (status) data account hashed-password callback))

(defn send-logs [dbJson js-logs callback]
  (log/debug "[native-module] send-logs")
  (.sendLogs ^js (status) dbJson js-logs callback))

(defn add-peer [enode on-result]
  (log/debug "[native-module] add-peer")
  (.addPeer ^js (status) enode on-result))

(defn close-application []
  (log/debug "[native-module] close-application")
  (.closeApplication ^js (status)))

(defn connection-change [type expensive?]
  (log/debug "[native-module] connection-change")
  (.connectionChange ^js (status) type (boolean expensive?)))

(defn app-state-change [state]
  (log/debug "[native-module] app-state-change")
  (.appStateChange ^js (status) state))

(defn set-blank-preview-flag [flag]
  (log/debug "[native-module] set-blank-preview-flag")
  (.setBlankPreviewFlag ^js (status) flag))

(defn is24Hour []
  (log/debug "[native-module] is24Hour")
  ;;NOTE: we have to check for status module because of tests
  (when (status)
    (.-is24Hour ^js (status))))

(defn get-device-model-info []
  (log/debug "[native-module] get-device-model-info")
  ;;NOTE: we have to check for status module because of tests
  (when-let [^js status (status)]
    {:model     (.-model status)
     :brand     (.-brand status)
     :build-id  (.-buildId status)
     :device-id (.-deviceId status)}))

(defn extract-group-membership-signatures
  [signature-pairs callback]
  (log/debug "[native-module] extract-group-membership-signatures")
  (.extractGroupMembershipSignatures ^js (status) signature-pairs callback))

(defn sign-group-membership [content callback]
  (log/debug "[native-module] sign-group-membership")
  (.signGroupMembership ^js (status) content callback))

(defn update-mailservers
  [enodes on-result]
  (log/debug "[native-module] update-mailservers")
  (.updateMailservers ^js (status) enodes on-result))

(defn chaos-mode-update [on on-result]
  (log/debug "[native-module] chaos-mode-update")
  (.chaosModeUpdate ^js (status) on on-result))

(defn get-nodes-from-contract
  [rpc-endpoint contract-address on-result]
  (log/debug "[native-module] get-nodes-from-contract")
  (.getNodesFromContract ^js (status) rpc-endpoint contract-address on-result))

(defn rooted-device? [callback]
  (log/debug "[native-module] rooted-device?")
  (cond
    ;; we assume that iOS is safe by default
    platform/ios?
    (callback false)

    ;; we check root on android
    platform/android?
    (if (status)
      (.isDeviceRooted ^js (status) callback)
      ;; if module isn't initialized we return true to avoid degrading security
      (callback true))

    ;; in unknown scenarios we also consider the device rooted to avoid degrading security
    :else (callback true)))

(defn generate-gfycat
  "Generate a 3 words random name based on the user public-key, synchronously"
  [public-key]
  {:pre [(utils.db/valid-public-key? public-key)]}
  (log/debug "[native-module] generate-gfycat")
  (.generateAlias ^js (status) public-key))

(defn generate-gfycat-async
  "Generate a 3 words random name based on the user public-key, asynchronously"
  [public-key callback]
  {:pre [(utils.db/valid-public-key? public-key)]}
  (.generateAliasAsync ^js (status) public-key callback))

(defn identicon
  "Generate a icon based on a string, synchronously"
  [seed]
  (log/debug "[native-module] identicon")
  (.identicon ^js (status) seed))

(defn identicon-async
  "Generate a icon based on a string, asynchronously"
  [seed callback]
  (.identiconAsync ^js (status) seed callback))

(defn gfycat-identicon-async
  "Generate an icon based on a string and 3 words random name asynchronously"
  [seed callback]
  (log/debug "[native-module] gfycat-identicon-async")
  (.generateAliasAndIdenticonAsync ^js (status) seed callback))

(defn validate-mnemonic
  "Validate that a mnemonic conforms to BIP39 dictionary/checksum standards"
  [mnemonic callback]
  (log/debug "[native-module] validate-mnemonic")
  (.validateMnemonic ^js (status) mnemonic callback))

(defn delete-multiaccount
  "Delete multiaccount from database, deletes multiaccount's database and
  key files."
  [key-uid callback]
  (log/debug "[native-module] delete-multiaccount")
  (.deleteMultiaccount ^js (status) key-uid callback))

(defn activate-keep-awake []
  (log/debug "[native-module] activateKeepAwake")
  (.activateKeepAwake ^js (status)))

(defn deactivate-keep-awake []
  (log/debug "[native-module] deactivateKeepAwake")
  (.deactivateKeepAwake ^js (status)))
