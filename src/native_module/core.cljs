(ns native-module.core
  (:require ["react-native" :as react-native]
            [utils.validators :as validators]
            [taoensso.timbre :as log]
            [react-native.platform :as platform]
            [react-native.core :as rn]
            [utils.transforms :as types]
            [clojure.string :as string]))

(defn status
  []
  (when (exists? (.-NativeModules react-native))
    (.-Status ^js (.-NativeModules react-native))))

(defn init
  [handler]
  (.addListener ^js rn/device-event-emitter "gethEvent" #(handler (.-jsonEvent ^js %))))

(defn clear-web-data
  []
  (log/debug "[native-module] clear-web-data")
  (when (status)
    (.clearCookies ^js (status))
    (.clearStorageAPIs ^js (status))))

(defn init-keystore
  [key-uid callback]
  (log/debug "[native-module] init-keystore" key-uid)
  (.initKeystore ^js (status) key-uid callback))

(defn open-accounts
  [callback]
  (log/debug "[native-module] open-accounts")
  (.openAccounts ^js (status) #(callback (types/json->clj %))))

(defn prepare-dir-and-update-config
  [key-uid config callback]
  (log/debug "[native-module] prepare-dir-and-update-config")
  (.prepareDirAndUpdateConfig ^js (status)
                              key-uid
                              config
                              #(callback (types/json->clj %))))

(defn save-account-and-login
  "NOTE: beware, the password has to be sha3 hashed"
  [key-uid multiaccount-data hashed-password settings config accounts-data]
  (log/debug "[native-module] save-account-and-login"
             "multiaccount-data"
             multiaccount-data)
  (clear-web-data)
  (init-keystore
   key-uid
   #(.saveAccountAndLogin
     ^js (status)
     multiaccount-data
     hashed-password
     settings
     config
     accounts-data)))

(defn save-multiaccount-and-login-with-keycard
  "NOTE: chat-key is a whisper private key sent from keycard"
  [key-uid multiaccount-data password settings config accounts-data chat-key]
  (log/debug "[native-module] save-account-and-login-with-keycard")
  (init-keystore
   key-uid
   #(.saveAccountAndLoginWithKeycard
     ^js (status)
     multiaccount-data
     password
     settings
     config
     accounts-data
     chat-key)))

(defn login-with-config
  "NOTE: beware, the password has to be sha3 hashed"
  [key-uid account-data hashed-password config]
  (log/debug "[native-module] loginWithConfig")
  (clear-web-data)
  (let [config (if config (types/clj->json config) "")]
    (init-keystore
     key-uid
     #(.loginWithConfig ^js (status) account-data hashed-password config))))

(defn login-account
  "NOTE: beware, the password has to be sha3 hashed"
  [{:keys [keyUid] :as request}]
  (log/debug "[native-module] loginWithConfig")
  (clear-web-data)
  (init-keystore
   keyUid
   #(.loginAccount ^js (status) (types/clj->json request))))

(defn create-account-and-login
  [request]
  (.createAccountAndLogin ^js (status) (types/clj->json request)))

(defn restore-account-and-login
  [request]
  (.restoreAccountAndLogin ^js (status) (types/clj->json request)))

(defn export-db
  "NOTE: beware, the password has to be sha3 hashed"
  [key-uid account-data hashed-password callback]
  (log/debug "[native-module] export-db")
  (clear-web-data)
  (init-keystore
   key-uid
   #(.exportUnencryptedDatabase ^js (status) account-data hashed-password callback)))

(defn import-db
  "NOTE: beware, the password has to be sha3 hashed"
  [key-uid account-data hashed-password]
  (log/debug "[native-module] import-db")
  (clear-web-data)
  (init-keystore
   key-uid
   #(.importUnencryptedDatabase ^js (status) account-data hashed-password)))

(defn logout
  []
  (log/debug "[native-module] logout")
  (clear-web-data)
  (.logout ^js (status)))

(defn multiaccount-load-account
  "NOTE: beware, the password has to be sha3 hashed

   this function is used after storing an account when you still want to
   derive accounts from it, because saving an account flushes the loaded keys
   from memory"
  [address hashed-password callback]
  (log/debug "[native-module] multiaccount-load-account")
  (.multiAccountLoadAccount ^js (status)
                            (types/clj->json {:address  address
                                              :password hashed-password})
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
                                                    :paths     paths})
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
     #(.multiAccountStoreAccount ^js (status)
                                 (types/clj->json {:accountID account-id
                                                   :password  hashed-password})
                                 callback))))

(defn multiaccount-store-derived
  "NOTE: beware, the password has to be sha3 hashed"
  [account-id key-uid paths hashed-password callback]
  (log/debug "[native-module] multiaccount-store-derived"
             "account-id"
             account-id)
  (init-keystore
   key-uid
   #(.multiAccountStoreDerived ^js (status)
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
  (.multiAccountGenerateAndDeriveAddresses ^js (status)
                                           (types/clj->json {:n                    n
                                                             :mnemonicPhraseLength mnemonic-length
                                                             :bip39Passphrase      ""
                                                             :paths                paths})
                                           callback))

(defn multiaccount-import-mnemonic
  [mnemonic password callback]
  (log/debug "[native-module] multiaccount-import-mnemonic")
  (.multiAccountImportMnemonic ^js (status)
                               (types/clj->json {:mnemonicPhrase  mnemonic
                                                 ;;NOTE this is not the multiaccount password
                                                 :Bip39Passphrase password})
                               callback))

(defn multiaccount-import-private-key
  [private-key callback]
  (log/debug "[native-module] multiaccount-import-private-key")
  (.multiAccountImportPrivateKey ^js (status)
                                 (types/clj->json {:privateKey private-key})
                                 callback))

(defn verify
  "NOTE: beware, the password has to be sha3 hashed"
  [address hashed-password callback]
  (log/debug "[native-module] verify")
  (.verify ^js (status) address hashed-password callback))

(defn verify-database-password
  "NOTE: beware, the password has to be sha3 hashed"
  [key-uid hashed-password callback]
  (log/debug "[native-module] verify-database-password")
  (.verifyDatabasePassword ^js (status) key-uid hashed-password callback))

(defn login-with-keycard
  [{:keys [key-uid multiaccount-data password chat-key node-config]}]
  (log/debug "[native-module] login-with-keycard")
  (clear-web-data)
  (init-keystore
   key-uid
   #(.loginWithKeycard ^js (status) multiaccount-data password chat-key (types/clj->json node-config))))

(defn set-soft-input-mode
  [mode]
  (log/debug "[native-module]  set-soft-input-mode")
  (.setSoftInputMode ^js (status) mode))

(defn call-rpc
  [payload callback]
  (log/debug "[native-module] call-rpc")
  (.callRPC ^js (status) payload callback))

(defn call-private-rpc
  [payload callback]
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

(defn start-searching-for-local-pairing-peers
  "starts a UDP multicast beacon that both listens for and broadcasts to LAN peers"
  [callback]
  (log/info "[native-module] Start Searching for Local Pairing Peers"
            {:fn :start-searching-for-local-pairing-peers})
  (.startSearchForLocalPairingPeers ^js (status) callback))

(defn local-pairing-preflight-outbound-check
  "Checks whether the device has allows connecting to the local server"
  [callback]
  (log/info "[native-module] Performing local pairing preflight check")
  (when platform/ios?
    (.localPairingPreflightOutboundCheck ^js (status) callback)))

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

(defn deserialize-and-compress-key
  "Provides a community id (public key) to status-go which is first deserialized
  and then compressed. Example input/output :
  input key  = zQ3shTAten2v9CwyQD1Kc7VXAqNPDcHZAMsfbLHCZEx6nFqk9 and
  output key = 0x025596a7ff87da36860a84b0908191ce60a504afc94aac93c1abd774f182967ce6"
  [input-key callback]
  (log/info "[native-module] Deserializing and then compressing public key"
            {:fn  :deserialize-and-compress-key
             :key input-key})
  (.deserializeAndCompressKey ^js (status) input-key callback))

(defn compressed-key->public-key
  "Provides compressed key to status-go and gets back the uncompressed public key via deserialization"
  [public-key deserialization-key callback]
  (log/info "[native-module] Deserializing compressed key"
            {:fn         :compressed-key->public-key
             :public-key public-key})
  (.multiformatDeserializePublicKey ^js (status) public-key deserialization-key callback))

(defn hash-typed-data
  "used for keycard"
  [data callback]
  (log/debug "[native-module] hash-typed-data")
  (.hashTypedData ^js (status) data callback))

(defn hash-typed-data-v4
  "used for keycard"
  [data callback]
  (log/debug "[native-module] hash-typed-data-v4")
  (.hashTypedDataV4 ^js (status) data callback))

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

(defn recover-message
  [rpcParams callback]
  (log/debug "[native-module] recover")
  (.recover ^js (status) rpcParams callback))

(defn send-transaction
  "NOTE: beware, the password has to be sha3 hashed"
  [rpcParams hashed-password callback]
  (log/debug "[native-module] send-transaction")
  (.sendTransaction ^js (status) rpcParams hashed-password callback))

(defn sign-typed-data
  "NOTE: beware, the password has to be sha3 hashed"
  [data account hashed-password callback]
  (log/debug "[native-module] sign-typed-data")
  (.signTypedData ^js (status) data account hashed-password callback))

(defn sign-typed-data-v4
  "NOTE: beware, the password has to be sha3 hashed"
  [data account hashed-password callback]
  (log/debug "[native-module] sign-typed-data-v4")
  (.signTypedDataV4 ^js (status) data account hashed-password callback))

(defn send-logs
  [dbJson js-logs callback]
  (log/debug "[native-module] send-logs")
  (.sendLogs ^js (status) dbJson js-logs callback))

(defn close-application
  []
  (log/debug "[native-module] close-application")
  (.closeApplication ^js (status)))

(defn connection-change
  [type expensive?]
  (log/debug "[native-module] connection-change")
  (.connectionChange ^js (status) type (boolean expensive?)))

(defn app-state-change
  [state]
  (log/debug "[native-module] app-state-change")
  (.appStateChange ^js (status) state))

(defn start-local-notifications
  []
  (log/debug "[native-module] start-local-notifications")
  (.startLocalNotifications ^js (status)))

(defn set-blank-preview-flag
  [flag]
  (log/debug "[native-module] set-blank-preview-flag")
  (.setBlankPreviewFlag ^js (status) flag))

(defn get-device-model-info
  []
  (log/debug "[native-module] get-device-model-info")
  ;;NOTE: we have to check for status module because of tests
  (when-let [^js status-module (status)]
    {:model     (.-model status-module)
     :brand     (.-brand status-module)
     :build-id  (.-buildId status-module)
     :device-id (.-deviceId status-module)}))

(defn get-installation-name
  []
  ;; NOTE(rasom): Only needed for android devices currently
  (when platform/android?
    (string/join " "
                 ((juxt :model :device-id)
                  (get-device-model-info)))))

(defn get-node-config
  [callback]
  (log/debug "[native-module] get-node-config")
  (.getNodeConfig ^js (status) callback))

(defn toggle-webview-debug
  [on]
  (log/debug "[native-module] toggle-webview-debug" on)
  (.toggleWebviewDebug ^js (status) on))

(defn rooted-device?
  [callback]
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
  (log/debug "[native-module] generate-gfycat")
  (when (validators/valid-public-key? public-key)
    (.generateAlias ^js (status) public-key)))

(defn identicon
  "Generate a icon based on a string, synchronously"
  [seed]
  (log/debug "[native-module] identicon")
  (.identicon ^js (status) seed))

(defn encode-transfer
  [to-norm amount-hex]
  (log/debug "[native-module] encode-transfer")
  (.encodeTransfer ^js (status) to-norm amount-hex))

(defn decode-parameters
  [bytes-string types]
  (log/debug "[native-module] decode-parameters")
  (let [json-str (.decodeParameters ^js (status)
                                    (types/clj->json {:bytesString bytes-string :types types}))]
    (types/json->clj json-str)))

(defn hex-to-number
  [hex]
  (log/debug "[native-module] hex-to-number")
  (let [json-str (.hexToNumber ^js (status) hex)]
    (types/json->clj json-str)))

(defn number-to-hex
  [num]
  (log/debug "[native-module] number-to-hex")
  (.numberToHex ^js (status) (str num)))

(defn sha3
  [s]
  (log/debug "[native-module] sha3")
  (.sha3 ^js (status) s))

(defn utf8-to-hex
  [s]
  (log/debug "[native-module] utf8-to-hex")
  (.utf8ToHex ^js (status) s))

(defn hex-to-utf8
  [s]
  (log/debug "[native-module] hex-to-utf8")
  (.hexToUtf8 ^js (status) s))

(defn check-address-checksum
  [address]
  (log/debug "[native-module] check-address-checksum")
  (let [result (.checkAddressChecksum ^js (status) address)]
    (types/json->clj result)))

(defn address?
  [address]
  (log/debug "[native-module] address?")
  (let [result (.isAddress ^js (status) address)]
    (types/json->clj result)))

(defn to-checksum-address
  [address]
  (log/debug "[native-module] to-checksum-address")
  (.toChecksumAddress ^js (status) address))

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

(defn delete-imported-key
  "Delete imported key file."
  [key-uid address hashed-password callback]
  (log/debug "[native-module] delete-imported-key")
  (.deleteImportedKey ^js (status) key-uid address hashed-password callback))

(defn reset-keyboard-input
  [input selection]
  (log/debug "[native-module] resetKeyboardInput")
  (when platform/android?
    (.resetKeyboardInputCursor ^js (status) input selection)))

;; passwords are hashed
(defn reset-password
  [key-uid current-password# new-password# callback]
  (log/debug "[native-module] change-database-password")
  (init-keystore
   key-uid
   #(.reEncryptDbAndKeystore ^js (status) key-uid current-password# new-password# callback)))

(defn convert-to-keycard-account
  [{:keys [key-uid] :as multiaccount-data} settings current-password# new-password callback]
  (log/debug "[native-module] convert-to-keycard-account")
  (.convertToKeycardAccount ^js (status)
                            key-uid
                            (types/clj->json multiaccount-data)
                            (types/clj->json settings)
                            ""
                            current-password#
                            new-password
                            callback))

(defn backup-disabled-data-dir
  []
  (.backupDisabledDataDir ^js (status)))

(defn keystore-dir
  []
  (.keystoreDir ^js (status)))

(defn log-file-directory
  []
  (.logFileDirectory ^js (status)))

(defn init-status-go-logging
  [{:keys [enable? mobile-system? log-level callback]}]
  (.initLogging ^js (status) enable? mobile-system? log-level callback))
