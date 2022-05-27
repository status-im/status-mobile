(ns status-im.utils.test
  (:require [re-frame.core :as re-frame]))

(def native-status (js/require "../../modules/react-native-status/nodejs/bindings"))

(def test-dir "/tmp")

(defn signal-received-callback [a]
  (re-frame/dispatch [:signals/signal-received a]))

;; We poll for signals, could not get callback working
(defn init! []
  (.setSignalEventCallback native-status)
  (js/setInterval (fn []
                    (.pollSignal native-status signal-received-callback) 100)))

(def status
  (clj->js {:openAccounts (fn [callback]
                            (callback
                             (.openAccounts
                              native-status
                              test-dir)))
            :multiAccountStoreDerived (fn [json callback]
                                        (callback (.multiAccountStoreDerivedAccounts
                                                   native-status
                                                   json)))
            :clearCookies identity
            :clearStorageAPIs identity
            :setBlankPreviewFlag identity
            :callPrivateRPC (fn [payload callback]
                              (callback
                               (.callPrivateRPC
                                native-status
                                payload)))
            :saveAccountAndLogin (fn [multiaccount-data password settings config accounts-data]
                                   (.saveAccountAndLogin
                                    native-status
                                    multiaccount-data
                                    password
                                    settings
                                    config
                                    accounts-data))

            :generateAliasAndIdenticonAsync (fn [seed callback]
                                              (let [generated-identicon (.identicon native-status seed)
                                                    generated-alias (.generateAlias native-status seed)]
                                                (callback generated-alias generated-identicon)))
            :multiAccountGenerateAndDeriveAddresses (fn [json callback]
                                                      (callback
                                                       (.multiAccountGenerateAndDeriveAddresses
                                                        native-status
                                                        json)))
            :initKeystore (fn [key-uid callback]
                            (callback
                             (.initKeystore
                              native-status
                              (str test-dir "/keystore/" key-uid))))

            :identicon (fn [pk]
                         (.identicon native-status pk))}))
