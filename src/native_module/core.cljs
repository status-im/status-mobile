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

(defn initialize-application
  [request callback]
  (log/debug "[native-module] initialize-application")
  (.initializeApplication ^js (account-manager)
                          (types/clj->json request)
                          #(callback (types/json->clj %))))

(defn accept-terms
  ([]
   (native-utils/promisify-native-module-call accept-terms))
  ([callback]
   (.acceptTerms ^js (account-manager) callback)))

(defn prepare-dir-and-update-config
  [key-uid config callback]
  (log/debug "[native-module] prepare-dir-and-update-config")
  (.prepareDirAndUpdateConfig ^js (account-manager)
                              key-uid
                              config
                              #(callback (types/json->clj %))))

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

(defn verify
  "NOTE: beware, the password has to be sha3 hashed"
  [address hashed-password callback]
  (log/debug "[native-module] verify")
  (.verify ^js (account-manager) address hashed-password callback))

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

(defn serialize-legacy-key
  "Compresses an old format public key (0x04...) to the new one zQ..."
  [public-key]
  (.serializeLegacyKey ^js (encryption) public-key))

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
  ([rpcParams]
   (native-utils/promisify-native-module-call sign-message rpcParams))
  ([rpcParams callback]
   (log/debug "[native-module] sign-message")
   (.signMessage ^js (encryption) rpcParams callback)))

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
  ([data account hashed-password]
   (native-utils/promisify-native-module-call sign-typed-data data account hashed-password))
  ([data account hashed-password callback]
   (log/debug "[native-module] sign-typed-data")
   (.signTypedData ^js (encryption) data account hashed-password callback)))

(defn sign-typed-data-v4
  "NOTE: beware, the password has to be sha3 hashed"
  ([data account hashed-password]
   (native-utils/promisify-native-module-call sign-typed-data-v4 data account hashed-password))
  ([data account hashed-password callback]
   (log/debug "[native-module] sign-typed-data-v4")
   (.signTypedDataV4 ^js (encryption) data account hashed-password callback)))

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
  ;; Sometimes the app crashes during logout because `flag` is nil.
  (when-not (nil? flag)
    (.setBlankPreviewFlag ^js (encryption) flag)))

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

(defn encode-function-call
  [method params]
  (log/debug "[native-module] encode-function-call")
  (.encodeFunctionCall ^js (encryption) method (types/clj->json params)))

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

(defn toggle-centralized-metrics
  [enabled callback]
  (.toggleCentralizedMetrics ^js (status) (types/clj->json {:enabled enabled}) callback))

(defn add-centralized-metric
  [metric]
  (.addCentralizedMetric ^js (status) (types/clj->json metric) #(log/debug "pushed metric" % metric)))

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
  ([mnemonic]
   (native-utils/promisify-native-module-call validate-mnemonic mnemonic))
  ([mnemonic callback]
   (log/debug "[native-module] validate-mnemonic")
   (.validateMnemonic ^js (utils) mnemonic callback)))

(defn validate-connection-string
  [connection-string]
  (log/debug "[native-module] validate-connection-string")
  (->> connection-string
       (.validateConnectionString ^js (utils))
       types/json->clj))

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

(defn convert-to-keycard-profile
  [{:keys [key-uid] :as profile} settings password new-password callback]
  (.convertToKeycardAccount ^js (encryption)
                            key-uid
                            (types/clj->json profile)
                            (types/clj->json settings)
                            (:keycard-instance-uid settings)
                            password
                            new-password
                            #(when callback (callback (types/json->clj %)))))

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
  [{:keys [enable? mobile-system? log-level log-request-go? callback]}]
  (.initLogging ^js (log-manager) enable? mobile-system? log-level log-request-go? callback))

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
  ([config-json]
   (native-utils/promisify-native-module-call get-connection-string-for-exporting-keypairs-keystores
                                              config-json))
  ([config-json callback]
   (log/info "[native-module] Fetching Export Keypairs Connection String"
             {:fn          :get-connection-string-for-exporting-keypairs-keystores
              :config-json config-json})
   (.getConnectionStringForExportingKeypairsKeystores ^js (network) config-json callback)))

(defn input-connection-string-for-importing-keypairs-keystores
  "Provides connection string to status-go for the purpose of importing keypairs and keystores on the receiver side"
  ([connection-string config-json]
   (native-utils/promisify-native-module-call input-connection-string-for-importing-keypairs-keystores
                                              connection-string
                                              config-json))
  ([connection-string config-json callback]
   (log/info "[native-module] Sending Import Keypairs Connection String"
             {:fn                :input-connection-string-for-importing-keypairs-keystores
              :config-json       config-json
              :connection-string connection-string})
   (.inputConnectionStringForImportingKeypairsKeystores ^js (network)
                                                        connection-string
                                                        config-json
                                                        callback)))

(defn create-account-from-private-key
  "Validate that a mnemonic conforms to BIP39 dictionary/checksum standards"
  ([private-key]
   (native-utils/promisify-native-module-call create-account-from-private-key private-key))
  ([private-key callback]
   (log/debug "[native-module] create-account-from-private-key")
   (.createAccountFromPrivateKey ^js (account-manager)
                                 (types/clj->json {:privateKey private-key})
                                 callback)))
