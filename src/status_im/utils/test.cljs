(ns status-im.utils.test
  (:require [re-frame.core :as re-frame]))

(def native-status (js/require "../../modules/react-native-status/nodejs/bindings"))

(def fs (js/require "fs"))
(def path (js/require "path"))
(def os (js/require "os"))

(def tmpdir (.tmpdir os))

(def test-dir-prefix (.join path tmpdir "status-mobile-tests"))

(def test-dir (.mkdtempSync fs test-dir-prefix))

(defn signal-received-callback
  [a]
  (re-frame/dispatch [:signals/signal-received a]))

;; We poll for signals, could not get callback working
(defn init!
  []
  (.setSignalEventCallback native-status)
  (js/setInterval (fn []
                    (.pollSignal native-status signal-received-callback)
                    100)))

(def status
  (clj->js
   {:openAccounts
    (fn [callback]
      (callback (.openAccounts native-status test-dir)))

    :multiAccountStoreDerived
    (fn [json callback]
      (callback (.multiAccountStoreDerivedAccounts native-status json)))

    :clearCookies identity

    :clearStorageAPIs identity

    :setBlankPreviewFlag identity

    :callPrivateRPC
    (fn [payload callback]
      (callback (.callPrivateRPC native-status payload)))

    :saveAccountAndLogin
    (fn [multiaccount-data password settings config accounts-data]
      (.saveAccountAndLogin native-status multiaccount-data password settings config accounts-data))

    :logout
    (fn [] (.logout native-status))

    :generateAlias
    (fn [seed] (.generateAlias native-status seed))

    :generateAliasAndIdenticonAsync
    (fn [seed callback]
      (let [generated-identicon (.identicon native-status seed)
            generated-alias     (.generateAlias native-status seed)]
        (callback generated-alias generated-identicon)))

    :multiAccountGenerateAndDeriveAddresses
    (fn [json callback]
      (callback (.multiAccountGenerateAndDeriveAddresses native-status json)))

    :multiAccountImportMnemonic
    (fn [json callback]
      (callback (.multiAccountImportMnemonic native-status json)))

    :multiAccountLoadAccount
    (fn [json callback]
      (callback (.multiAccountLoadAccount native-status json)))

    :multiAccountDeriveAddresses
    (fn [json callback]
      (callback (.multiAccountDeriveAddresses native-status json)))

    :multiformatDeserializePublicKey
    (fn [public-key deserialization-key callback]
      (callback (.multiformatDeserializePublicKey
                 native-status
                 public-key
                 deserialization-key)))

    :initKeystore
    (fn [key-uid callback]
      (callback (.initKeystore native-status
                               (str test-dir "/keystore/" key-uid))))

    :identicon
    (fn [pk] (.identicon native-status pk))

    :encodeTransfer
    (fn [to-norm amount-hex]
      (.encodeTransfer native-status to-norm amount-hex))

    :encodeFunctionCall
    (fn [method params-json]
      (.encodeFunctionCall native-status method params-json))

    :decodeParameters
    (fn [decode-param-json]
      (.decodeParameters native-status decode-param-json))

    :hexToNumber
    (fn [hex] (.hexToNumber native-status hex))

    :numberToHex
    (fn [num-str] (.numberToHex native-status num-str))

    :checkAddressChecksum
    (fn [address] (.checkAddressChecksum native-status address))

    :sha3
    (fn [str] (.sha3 native-status str))

    :toChecksumAddress
    (fn [address] (.toChecksumAddress native-status address))

    :isAddress
    (fn [address] (.isAddress native-status address))

    :validateMnemonic
    (fn [json callback] (callback (.validateMnemonic native-status json)))

    :startLocalNotifications identity}))
