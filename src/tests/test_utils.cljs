(ns tests.test-utils
  (:require
    [legacy.status-im.utils.deprecated-types :as types]
    [re-frame.core :as re-frame]))

(def native-status (js/require "../../modules/react-native-status/nodejs/bindings"))

(def fs (js/require "fs"))
(def path (js/require "path"))
(def os (js/require "os"))

(def tmpdir (.tmpdir os))

(def test-dir-prefix (.join path tmpdir "status-mobile-tests"))

(def test-dir (.mkdtempSync fs test-dir-prefix))

(def initialized? (atom false))

(defn signal-received-callback
  [a]
  (re-frame/dispatch [:signals/signal-received a]))

;; We poll for signals, could not get callback working
(defn init!
  []
  (when-not @initialized?
    (.setSignalEventCallback native-status)
    (reset! initialized? true)
    (js/setInterval (fn []
                      (.pollSignal native-status signal-received-callback)
                      100))))

(def ui-helper
  (clj->js
   {:clearCookies     identity
    :clearStorageAPIs identity}))

(def encryption-utils
  (clj->js
   {:sha3
    (fn [s] (.sha3 native-status s))
    :serializeLegacyKey
    (fn [s] (.serializeLegacyKey native-status s))
    :setBlankPreviewFlag
    identity
    :encodeTransfer
    (fn [to-norm amount-hex]
      (.encodeTransfer native-status to-norm amount-hex))
    :hexToNumber
    (fn [hex] (.hexToNumber native-status hex))
    :decodeParameters
    (fn [decode-param-json]
      (.decodeParameters native-status decode-param-json))
    :numberToHex
    (fn [num-str] (.numberToHex native-status num-str))
    :initKeystore
    (fn [key-uid callback]
      (callback (.initKeystore native-status
                               (str test-dir "/keystore/" key-uid))))
    :multiformatDeserializePublicKey
    (fn [public-key deserialization-key callback]
      (callback (.multiformatDeserializePublicKey
                 native-status
                 public-key
                 deserialization-key)))}))

(def account-manager
  (clj->js
   {:initializeApplication
    (fn [request callback]
      (callback (.initializeApplication native-status request)))
    :acceptTerms
    (fn [callback]
      (callback (.acceptTerms native-status)))
    :createAccountAndLogin
    (fn [request] (.createAccountAndLogin native-status request))
    :restoreAccountAndLogin
    (fn [request]
      (prn native-status)
      (.restoreAccountAndLogin native-status request))
    :loginAccount
    (fn [request] (.loginAccount native-status request))
    :logout
    (fn [] (.logout native-status))
    :multiAccountImportMnemonic
    (fn [json callback]
      (callback (.multiAccountImportMnemonic native-status json)))
    :multiAccountLoadAccount
    (fn [json callback]
      (callback (.multiAccountLoadAccount native-status json)))
    :multiAccountDeriveAddresses
    (fn [json callback]
      (callback (.multiAccountDeriveAddresses native-status json)))
    :multiAccountGenerateAndDeriveAddresses
    (fn [json callback]
      (callback (.multiAccountGenerateAndDeriveAddresses native-status json)))
    :multiAccountStoreDerived
    (fn [json callback]
      (callback (.multiAccountStoreDerivedAccounts native-status json)))}))

(def utils
  (clj->js
   {:backupDisabledDataDir
    (fn [] (str test-dir "/backup"))
    :keystoreDir (fn [] "")
    :toChecksumAddress
    (fn [address] (.toChecksumAddress native-status address))
    :checkAddressChecksum
    (fn [address] (.checkAddressChecksum native-status address))
    :validateMnemonic
    (fn [json callback] (callback (.validateMnemonic native-status json)))
    :isAddress
    (fn [address] (.isAddress native-status address))}))

(def log-manager
  (clj->js
   {:logFileDirectory
    (fn [] (str test-dir "/log"))}))

(def network
  (clj->js
   {:callPrivateRPC
    (fn [payload callback]
      (callback (.callPrivateRPC native-status payload)))}))

(def status
  (clj->js
   {:getNodeConfig
    (fn [] (types/clj->json {:WakuV2Config ""}))
    :addCentralizedMetric
    (fn [_ callback]
      (callback))
    :fleets
    (fn [] (.fleets native-status))
    :startLocalNotifications
    identity}))
