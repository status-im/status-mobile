(ns status-im.utils.keychain.core
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.react-native.js-dependencies :as rn]
            [status-im.utils.platform :as platform]
            [status-im.utils.security :as security]
            [status-im.native-module.core :as status]
            [status-im.utils.fx :as fx]
            [goog.object :as object]
            [clojure.string :as string]))

(defn- check-conditions [callback & checks]
  (if (= (count checks) 0)
    (callback true)
    (let [current-check-fn (first checks)
          process-check-result (fn [callback-success callback-fail]
                                 (fn [current-check-passed?]
                                   (if current-check-passed?
                                     (callback-success)
                                     (callback-fail))))]
      (current-check-fn (process-check-result
                         #(apply (partial check-conditions callback) (rest checks))
                         #(callback false))))))

;; ********************************************************************************
;; Storing / Retrieving a user password to/from Keychain
;; ********************************************************************************
;;
;; We are using set/get/reset internet credentials there because they are bound
;; to an address (`server`) property.

(defn enum-val [enum-name value-name]
  (-> rn/keychain
      (object/get enum-name)
      (object/get value-name)))

;; We need a more strict access mode for keychain entries that save user password.
;; iOS
;;   see this article for more details:
;;   https://developer.apple.com/documentation/security/keychain_services/keychain_items/restricting_keychain_item_accessibility?language=objc
(def keychain-restricted-availability
  ;; From Apple's documentation:
  ;; > The kSecAttrAccessible attribute enables you to control item availability
  ;; > relative to the lock state of the device.
  ;; > It also lets you specify eligibility for restoration to a new device.
  ;; > If the attribute ends with the string ThisDeviceOnly,
  ;; > the item can be restored to the same device that created a backup,
  ;; > but it isn’t migrated when restoring another device’s backup data.
  ;; > ...
  ;; > For extremely sensitive data
  ;; > THAT YOU NEVER WANT STORED IN iCloud,
  ;; > you might choose kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly.
  ;; That is exactly what we use there.
  ;; Note that the password won't be stored if the device isn't locked by a passcode.
  #js {:accessible (enum-val "ACCESSIBLE" "WHEN_PASSCODE_SET_THIS_DEVICE_ONLY")})

(def ^:const keychain-secure-hardware
  ;; (Android) Requires storing the encryption key for the entry in secure hardware
  ;; or StrongBox (see https://developer.android.com/training/articles/keystore#ExtractionPrevention)
  "SECURE_HARDWARE")

;; These helpers check if the device is okay to use for password storage
;; They resolve callback with `true` if the check is passed, with `false` otherwise.
;; Android only
(defn- device-not-rooted? [callback]
  (status/rooted-device? (fn [rooted?] (callback (not rooted?)))))

;; Android only
(defn- secure-hardware-available? [callback]
  (-> (.getSecurityLevel rn/keychain)
      (.then (fn [level] (callback (= level keychain-secure-hardware))))))

;; iOS only
(defn- device-encrypted? [callback]
  (-> (.canImplyAuthentication
       rn/keychain
       #js {:authenticationType (enum-val "ACCESS_CONTROL" "BIOMETRY_ANY_OR_DEVICE_PASSCODE")})
      (.then callback)))

(defn- whisper-key-name [address]
  (str address "-whisper"))

(defn can-save-user-password? [callback]
  (log/debug "[keychain] can-save-user-password?")
  (cond
    platform/ios?
    (check-conditions callback device-encrypted?)

    platform/android?
    (check-conditions callback secure-hardware-available? device-not-rooted?)

    :else
    (callback false)))

(defn save-credentials
  "Stores the credentials for the address to the Keychain"
  [server username password callback]
  (log/debug "[keychain] save-credentials")
  (-> (.setInternetCredentials rn/keychain (string/lower-case server) username password
                               keychain-secure-hardware keychain-restricted-availability)
      (.then callback)))

(defn get-credentials
  "Gets the credentials for a specified server from the Keychain"
  [server callback]
  (log/debug "[keychain] get-credentials")
  (if platform/mobile?
    (-> (.getInternetCredentials rn/keychain (string/lower-case server))
        (.then callback))
    (callback))) ;; no-op for Desktop

(def ^:const auth-method-password "password")
(def ^:const auth-method-biometric "biometric")
(def ^:const auth-method-biometric-prepare "biometric-prepare")
(def ^:const auth-method-none "none")

(re-frame/reg-fx
 :keychain/get-auth-method
 (fn [[address callback]]
   (can-save-user-password?
    (fn [can-save?]
      (if can-save?
        (get-credentials (str address "-auth")
                         #(callback (if %
                                      (.-password %)
                                      auth-method-none)))
        (callback nil))))))

(re-frame/reg-fx
 :keychain/get-user-password
 (fn [[address callback]]
   (get-credentials address #(if % (callback (security/mask-data (.-password %))) (callback nil)))))

(re-frame/reg-fx
 :keychain/get-hardwallet-keys
 (fn [[address callback]]
   (get-credentials
    address
    (fn [encryption-key-data]
      (if encryption-key-data
        (get-credentials
         (whisper-key-name address)
         (fn [whisper-key-data]
           (if whisper-key-data
             (callback [(.-password encryption-key-data)
                        (.-password whisper-key-data)])
             (callback nil))))
        (callback nil))))))

(re-frame/reg-fx
 :keychain/save-user-password
 (fn [[address password]]
   (save-credentials
    address
    address
    (security/safe-unmask-data password)
    #(when-not %
       (log/error
        (str "Error while saving password."
             " "
             "The app will continue to work normally, "
             "but you will have to login again next time you launch it."))))))

(re-frame/reg-fx
 :keychain/save-auth-method
 (fn [[address method]]
   (log/debug "[keychain] :keychain/save-auth-method"
              "method" method)
   (save-credentials
    (str address "-auth")
    address
    method
    #(when-not %
       (log/error
        (str "Error while saving auth method."
             " "
             "The app will continue to work normally, "
             "but you will have to login again next time you launch it."))))))

(re-frame/reg-fx
 :keychain/save-hardwallet-keys
 (fn [[address encryption-public-key whisper-private-key]]
   (save-credentials
    address
    address
    encryption-public-key
    #(when-not %
       (log/error
        (str "Error while saving encryption-public-key"))))
   (save-credentials
    (whisper-key-name address)
    address
    whisper-private-key
    #(when-not %
       (log/error
        (str "Error while saving whisper-private-key"))))))

(re-frame/reg-fx
 :keychain/clear-user-password
 (fn [address]
   (when platform/mobile?
     (-> (.resetInternetCredentials rn/keychain (string/lower-case address))
         (.then #(when-not % (log/error (str "Error while clearing saved password."))))))))

(fx/defn get-auth-method
  [_ address]
  {:keychain/get-auth-method
   [address #(re-frame/dispatch [:multiaccounts.login/get-auth-method-success % address])]})

(fx/defn get-user-password
  [_ address]
  {:keychain/get-user-password
   [address
    #(re-frame/dispatch
      [:multiaccounts.login.callback/get-user-password-success % address])]})

(fx/defn get-hardwallet-keys
  [_ address]
  {:keychain/get-hardwallet-keys
   [address
    #(re-frame/dispatch
      [:multiaccounts.login.callback/get-hardwallet-keys-success address %])]})

(fx/defn save-user-password
  [_ address password]
  {:keychain/save-user-password [address password]})

(fx/defn save-hardwallet-keys
  [_ address encryption-public-key whisper-private-key]
  {:keychain/save-hardwallet-keys [address
                                   encryption-public-key
                                   whisper-private-key]})
(fx/defn save-auth-method
  [{:keys [db]} address method]
  {:db                        (assoc db :auth-method method)
   :keychain/save-auth-method [address method]})
