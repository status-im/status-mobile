(ns status-im.utils.keychain.core
  (:require [react-native.keychain :as keychain]
            [re-frame.core :as re-frame]
            [utils.re-frame :as rf]
            [taoensso.timbre :as log]
            [oops.core :as oops]))

(def auth-method-biometric "biometric")
(def auth-method-biometric-prepare "biometric-prepare")
(def auth-method-none "none")

(defn- whisper-key-name
  [address]
  (str address "-whisper"))

(re-frame/reg-fx
 :keychain/get-keycard-keys
 (fn [[key-uid callback]]
   (keychain/get-credentials
    key-uid
    (fn [encryption-key-data]
      (if encryption-key-data
        (keychain/get-credentials
         (whisper-key-name key-uid)
         (fn [whisper-key-data]
           (if whisper-key-data
             (callback [(oops/oget encryption-key-data "password")
                        (oops/oget whisper-key-data "password")])
             (callback nil))))
        (callback nil))))))

(re-frame/reg-fx
 :keychain/save-keycard-keys
 (fn [[key-uid encryption-public-key whisper-private-key]]
   (keychain/save-credentials
    key-uid
    key-uid
    encryption-public-key
    #(when-not %
       (log/error
        (str "Error while saving encryption-public-key"))))
   (keychain/save-credentials
    (whisper-key-name key-uid)
    key-uid
    whisper-private-key
    #(when-not %
       (log/error
        (str "Error while saving whisper-private-key"))))))

(rf/defn get-keycard-keys
  [_ key-uid]
  {:keychain/get-keycard-keys
   [key-uid
    #(re-frame/dispatch
      [:multiaccounts.login.callback/get-keycard-keys-success key-uid %])]})

(rf/defn save-keycard-keys
  [_ key-uid encryption-public-key whisper-private-key]
  {:keychain/save-keycard-keys [key-uid
                                encryption-public-key
                                whisper-private-key]})
