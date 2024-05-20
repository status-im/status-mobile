(ns native-module.core
  (:require
    ["react-native" :as react-native]
    [clojure.string :as string]
    [native-module.utils :as native-utils]
    [react-native.platform :as platform]
    [taoensso.timbre :as log]
    [utils.transforms :as types]))

(defn status
  []
  (when (exists? (.-NativeModules react-native))
    (.-Status ^js (.-NativeModules react-native))))

(defn account-manager
  []
  (when (exists? (.-NativeModules react-native))
    (.-AccountManager ^js (.-NativeModules react-native))))

(defn encryption
  []
  (when (exists? (.-NativeModules react-native))
    (.-EncryptionUtils ^js (.-NativeModules react-native))))

(defn database
  []
  (when (exists? (.-NativeModules react-native))
    (.-DatabaseManager ^js (.-NativeModules react-native))))

(defn ui-helper
  []
  (when (exists? (.-NativeModules react-native))
    (.-UIHelper ^js (.-NativeModules react-native))))

(defn log-manager
  []
  (when (exists? (.-NativeModules react-native))
    (.-LogManager ^js (.-NativeModules react-native))))

(defn utils
  []
  (when (exists? (.-NativeModules react-native))
    (.-Utils ^js (.-NativeModules react-native))))

(defn network
  []
  (when (exists? (.-NativeModules react-native))
    (.-NetworkManager ^js (.-NativeModules react-native))))

(defn mail-manager
  []
  (when (exists? (.-NativeModules react-native))
    (.-MailManager ^js (.-NativeModules react-native))))

(defn mail
  [opts callback]
  (.mail ^js (mail-manager) (clj->js opts) callback))

(defn init
  [handler]
  (.addListener ^js (.-DeviceEventEmitter ^js react-native) "gethEvent" #(handler (.-jsonEvent ^js %))))

(defn clear-web-data
  []
  (log/debug "[native-module] clear-web-data")
  (when (ui-helper)
    (.clearCookies ^js (ui-helper))
    (.clearStorageAPIs ^js (ui-helper))))

(defn init-keystore
  [key-uid callback]
  (log/debug "[native-module] init-keystore" key-uid)
  (.initKeystore ^js (encryption) key-uid callback))

(defn open-accounts
  [callback]
  (log/debug "[native-module] open-accounts")
  (.openAccounts ^js (account-manager) #(callback (types/json->clj %))))

(defn prepare-dir-and-update-config
  [key-uid config callback]
  (log/debug "[native-module] prepare-dir-and-update-config")
  (.prepareDirAndUpdateConfig ^js (account-manager)
                              key-uid
                              config
                              #(callback (types/json->clj %))))

(defn save-multiaccount-and-login-with-keycard
  "NOTE: chat-key is a whisper private key sent from keycard"
  [key-uid multiaccount-data password settings config accounts-data chat-key]
  (log/debug "[native-module] save-account-and-login-with-keycard")
  (init-keystore
   key-uid
   #(.saveAccountAndLoginWithKeycard
     ^js (account-manager)
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
     #(.loginWithConfig ^js (account-manager) account-data hashed-password config))))

(defn login-account
  "NOTE: beware, the password has to be sha3 hashed"
  [{:keys [keyUid] :as request}]
  (log/debug "[native-module] loginAccount")
  (clear-web-data)
  (init-keystore
   keyUid
   #(.loginAccount ^js (account-manager) (types/clj->json request))))

(defn create-account-and-login
  [request]
  (.createAccountAndLogin ^js (account-manager) (types/clj->json request)))

(defn restore-account-and-login
  [request]
  (.restoreAccountAndLogin ^js (account-manager) (types/clj->json request)))

(defn export-db
  "NOTE: beware, the password has to be sha3 hashed"
  [key-uid account-data hashed-password callback]
  (log/debug "[native-module] export-db")
  (clear-web-data)
  (init-keystore
   key-uid
   #(.exportUnencryptedDatabase ^js (database) account-data hashed-password callback)))

(defn import-db
  "NOTE: beware, the password has to be sha3 hashed"
  [key-uid account-data hashed-password]
  (log/debug "[native-module] import-db")
  (clear-web-data)
  (init-keystore
   key-uid
   #(.importUnencryptedDatabase ^js (database) account-data hashed-password)))

(defn logout
  []
  (log/debug "[native-module] logout")
  (clear-web-data)
  (.logout ^js (account-manager)))

(defn multiaccount-load-account
  "NOTE: beware, the password has to be sha3 hashed

   this function is used after storing an account when you still want to
   derive accounts from it, because saving an account flushes the loaded keys
   from memory"
  [address hashed-password callback]
  (log/debug "[native-module] multiaccount-load-account")
  (.multiAccountLoadAccount ^js (account-manager)
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
    (.multiAccountDeriveAddresses ^js (account-manager)
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
     #(.multiAccountStoreAccount ^js (account-manager)
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
   #(.multiAccountStoreDerived ^js (account-manager)
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
  (.multiAccountGenerateAndDeriveAddresses ^js (account-manager)
                                           (types/clj->json {:n                    n
                                                             :mnemonicPhraseLength mnemonic-length
                                                             :bip39Passphrase      ""
                                                             :paths                paths})
                                           callback))

(defn multiaccount-import-mnemonic
  [mnemonic password callback]
  (log/debug "[native-module] multiaccount-import-mnemonic")
  (.multiAccountImportMnemonic ^js (account-manager)
                               (types/clj->json {:mnemonicPhrase  mnemonic
                                                 ;;NOTE this is not the multiaccount password
                                                 :Bip39Passphrase password})
                               callback))

(defn multiaccount-import-private-key
  [private-key callback]
  (log/debug "[native-module] multiaccount-import-private-key")
  (.multiAccountImportPrivateKey ^js (account-manager)
                                 (types/clj->json {:privateKey private-key})
                                 callback))

(defn verify
  "NOTE: beware, the password has to be sha3 hashed"
  [address hashed-password callback]
  (log/debug "[native-module] verify")
  (.verify ^js (account-manager) address hashed-password callback))

(defn verify-database-password
  "NOTE: beware, the password has to be sha3 hashed"
  [key-uid hashed-password callback]
  (log/debug "[native-module] verify-database-password")
  (.verifyDatabasePassword ^js (account-manager) key-uid hashed-password callback))

(defn login-with-keycard
  [{:keys [key-uid multiaccount-data password chat-key node-config]}]
  (log/debug "[native-module] login-with-keycard")
  (clear-web-data)
  (init-keystore
   key-uid
   #(.loginWithKeycard ^js (account-manager)
                       multiaccount-data
                       password
                       chat-key
                       (types/clj->json node-config))))

(defn set-soft-input-mode
  [mode]
  (log/debug "[native-module]  set-soft-input-mode")
  (.setSoftInputMode ^js (ui-helper) mode))

(defn call-rpc
  [payload callback]
  (log/debug "[native-module] call-rpc")
  (.callRPC ^js (network) payload callback))

(defn call-private-rpc
  [payload callback]
  (.callPrivateRPC ^js (network) payload callback))

(defn hash-transaction
  "used for keycard"
  [rpcParams callback]
  (log/debug "[native-module] hash-transaction")
  (.hashTransaction ^js (encryption) rpcParams callback))

(defn hash-message
  "used for keycard"
  [message callback]
  (log/debug "[native-module] hash-message")
  (.hashMessage ^js (encryption) message callback))

(defn start-searching-for-local-pairing-peers
  "starts a UDP multicast beacon that both listens for and broadcasts to LAN peers"
  [callback]
  (log/info "[native-module] Start Searching for Local Pairing Peers"
            {:fn :start-searching-for-local-pairing-peers})
  (.startSearchForLocalPairingPeers ^js (network) callback))

(defn local-pairing-preflight-outbound-check
  "Checks whether the device has allows connecting to the local server"
  [callback]
  (log/info "[native-module] Performing local pairing preflight check")
  (when platform/ios?
    (.localPairingPreflightOutboundCheck ^js (encryption) callback)))

(defn get-connection-string-for-bootstrapping-another-device
  "Generates connection string form status-go for the purpose of local pairing on the sender end"
  [config-json callback]
  (log/info "[native-module] Fetching Connection String"
            {:fn          :get-connection-string-for-bootstrapping-another-device
             :config-json config-json})
  (.getConnectionStringForBootstrappingAnotherDevice ^js (network) config-json callback))

(defn input-connection-string-for-bootstrapping
  "Provides connection string to status-go for the purpose of local pairing on the receiver end"
  [connection-string config-json callback]
  (log/info "[native-module] Sending Connection String"
            {:fn                :input-connection-string-for-bootstrapping
             :config-json       config-json
             :connection-string connection-string})
  (.inputConnectionStringForBootstrapping ^js (network) connection-string config-json callback))

(defn deserialize-and-compress-key
  "Provides a community id (public key) to status-go which is first deserialized
  and then compressed. Example input/output :
  input key  = zQ3shTAten2v9CwyQD1Kc7VXAqNPDcHZAMsfbLHCZEx6nFqk9 and
  output key = 0x025596a7ff87da36860a84b0908191ce60a504afc94aac93c1abd774f182967ce6"
  [input-key callback]
  (log/info "[native-module] Deserializing and then compressing public key"
            {:fn  :deserialize-and-compress-key
             :key input-key})
  (.deserializeAndCompressKey ^js (encryption) input-key callback))

(defn compressed-key->public-key
  "Provides compressed key to status-go and gets back the uncompressed public key via deserialization"
  [public-key deserialization-key callback]
  (log/info "[native-module] Deserializing compressed key"
            {:fn         :compressed-key->public-key
             :public-key public-key})
  (.multiformatDeserializePublicKey ^js (encryption) public-key deserialization-key callback))

(defn hash-typed-data
  "used for keycard"
  [data callback]
  (log/debug "[native-module] hash-typed-data")
  (.hashTypedData ^js (encryption) data callback))

(defn hash-typed-data-v4
  "used for keycard"
  [data callback]
  (log/debug "[native-module] hash-typed-data-v4")
  (.hashTypedDataV4 ^js (encryption) data callback))

(defn send-transaction-with-signature
  "used for keycard"
  [rpcParams sig callback]
  (log/debug "[native-module] send-transaction-with-signature")
  (.sendTransactionWithSignature ^js (network) rpcParams sig callback))

(defn sign-message
  "NOTE: beware, the password in rpcParams has to be sha3 hashed"
  [rpcParams callback]
  (log/debug "[native-module] sign-message")
  (.signMessage ^js (encryption) rpcParams callback))

(defn recover-message
  [rpcParams callback]
  (log/debug "[native-module] recover")
  (.recover ^js (network) rpcParams callback))

(defn send-transaction
  "NOTE: beware, the password has to be sha3 hashed"
  [rpcParams hashed-password callback]
  (log/debug "[native-module] send-transaction")
  (.sendTransaction ^js (network) rpcParams hashed-password callback))

(defn sign-typed-data
  "NOTE: beware, the password has to be sha3 hashed"
  [data account hashed-password callback]
  (log/debug "[native-module] sign-typed-data")
  (.signTypedData ^js (encryption) data account hashed-password callback))

(defn sign-typed-data-v4
  "NOTE: beware, the password has to be sha3 hashed"
  [data account hashed-password callback]
  (log/debug "[native-module] sign-typed-data-v4")
  (.signTypedDataV4 ^js (encryption) data account hashed-password callback))

(defn send-logs
  [dbJson js-logs callback]
  (log/debug "[native-module] send-logs")
  (.sendLogs ^js (log-manager) dbJson js-logs callback))

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
  (.setBlankPreviewFlag ^js (encryption) flag))

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
  (.toggleWebviewDebug ^js (ui-helper) on))

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

(defn encode-transfer
  [to-norm amount-hex]
  (log/debug "[native-module] encode-transfer")
  (.encodeTransfer ^js (encryption) to-norm amount-hex))

(defn decode-parameters
  [bytes-string types]
  (log/debug "[native-module] decode-parameters")
  (let [json-str (.decodeParameters ^js (encryption)
                                    (types/clj->json {:bytesString bytes-string :types types}))]
    (types/json->clj json-str)))

(defn hex-to-number
  [hex]
  (log/debug "[native-module] hex-to-number")
  (let [json-str (.hexToNumber ^js (encryption) hex)]
    (types/json->clj json-str)))

(defn number-to-hex
  [num]
  (log/debug "[native-module] number-to-hex")
  (.numberToHex ^js (encryption) (str num)))

(defn sha3
  [s]
  (log/debug "[native-module] sha3")
  (when s
    (.sha3 ^js (encryption) (str s))))

(defn utf8-to-hex
  [s]
  (log/debug "[native-module] utf8-to-hex")
  (.utf8ToHex ^js (encryption) s))

(defn hex-to-utf8
  [s]
  (log/debug "[native-module] hex-to-utf8")
  (.hexToUtf8 ^js (encryption) s))

(defn check-address-checksum
  [address]
  (log/debug "[native-module] check-address-checksum")
  (let [result (.checkAddressChecksum ^js (utils) address)]
    (types/json->clj result)))

(defn address?
  [address]
  (log/debug "[native-module] address?")
  (when address
    (let [result (.isAddress ^js (utils) address)]
      (types/json->clj result))))

(defn to-checksum-address
  [address]
  (log/debug "[native-module] to-checksum-address")
  (.toChecksumAddress ^js (utils) address))

(defn validate-mnemonic
  "Validate that a mnemonic conforms to BIP39 dictionary/checksum standards"
  [mnemonic callback]
  (log/debug "[native-module] validate-mnemonic")
  (.validateMnemonic ^js (utils) mnemonic callback))

(defn delete-multiaccount
  "Delete multiaccount from database, deletes multiaccount's database and
  key files."
  [key-uid callback]
  (log/debug "[native-module] delete-multiaccount")
  (.deleteMultiaccount ^js (account-manager) key-uid callback))

(defn delete-imported-key
  "Delete imported key file."
  [key-uid address hashed-password callback]
  (log/debug "[native-module] delete-imported-key")
  (.deleteImportedKey ^js (status) key-uid address hashed-password callback))

(defn reset-keyboard-input
  [input selection]
  (log/debug "[native-module] resetKeyboardInput")
  (when platform/android?
    (.resetKeyboardInputCursor ^js (ui-helper) input selection)))

(defn reset-password
  ([key-uid current-password-hashed new-password-hashed]
   (native-utils/promisify-native-module-call reset-password
                                              key-uid
                                              current-password-hashed
                                              new-password-hashed))
  ([key-uid current-password-hashed new-password-hashed callback]
   (log/debug "[native-module] change-database-password")
   (init-keystore
    key-uid
    #(.reEncryptDbAndKeystore ^js (encryption)
                              key-uid
                              current-password-hashed
                              new-password-hashed
                              callback))))

(defn convert-to-keycard-account
  [{:keys [key-uid] :as multiaccount-data} settings current-password# new-password callback]
  (log/debug "[native-module] convert-to-keycard-account")
  (.convertToKeycardAccount ^js (encryption)
                            key-uid
                            (types/clj->json multiaccount-data)
                            (types/clj->json settings)
                            ""
                            current-password#
                            new-password
                            callback))

(defn backup-disabled-data-dir
  []
  (.backupDisabledDataDir ^js (utils)))

(defn fleets
  []
  (.fleets ^js (status)))

(defn keystore-dir
  []
  (.keystoreDir ^js (utils)))

(defn log-file-directory
  []
  (.logFileDirectory ^js (log-manager)))

(defn init-status-go-logging
  [{:keys [enable? mobile-system? log-level callback]}]
  (.initLogging ^js (log-manager) enable? mobile-system? log-level callback))

(defn get-random-mnemonic
  [callback]
  (.getRandomMnemonic ^js (account-manager) #(callback (types/json->clj %))))

(defn create-account-from-mnemonic
  [mnemonic callback]
  (.createAccountFromMnemonicAndDeriveAccountsForPaths ^js (account-manager)
                                                       (types/clj->json mnemonic)
                                                       #(callback (types/json->clj %))))

(defn get-connection-string-for-exporting-keypairs-keystores
  "Generates connection string form status-go for the purpose of exporting keypairs and keystores on sender side"
  [config-json callback]
  (log/info "[native-module] Fetching Export Keypairs Connection String"
            {:fn          :get-connection-string-for-exporting-keypairs-keystores
             :config-json config-json})
  (.getConnectionStringForExportingKeypairsKeystores ^js (network) config-json callback))
