(ns status-im.utils.keychain.core
  (:require ["react-native-keychain" :as react-native-keychain]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [utils.re-frame :as rf]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]
            [utils.security.core :as security]))

(defn- check-conditions
  [callback & checks]
  (if (= (count checks) 0)
    (callback true)
    (let [current-check-fn     (first checks)
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

(defn enum-val
  [enum-name value-name]
  (get-in (js->clj react-native-keychain) [enum-name value-name]))

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

(def keychain-secure-hardware
  ;; (Android) Requires storing the encryption key for the entry in secure hardware
  ;; or StrongBox (see https://developer.android.com/training/articles/keystore#ExtractionPrevention)
  "SECURE_HARDWARE")

;; These helpers check if the device is okay to use for password storage
;; They resolve callback with `true` if the check is passed, with `false` otherwise.
;; Android only
(defn- device-not-rooted?
  [callback]
  (status/rooted-device? (fn [rooted?] (callback (not rooted?)))))

;; Android only
(defn- secure-hardware-available?
  [callback]
  (-> (.getSecurityLevel react-native-keychain)
      (.then (fn [level] (callback (= level keychain-secure-hardware))))))

;; iOS only
(defn- device-encrypted?
  [callback]
  (-> (.canImplyAuthentication
       react-native-keychain
       (clj->js
        {:authenticationType
         (enum-val "ACCESS_CONTROL" "BIOMETRY_ANY_OR_DEVICE_PASSCODE")}))
      (.then callback)))

(defn- whisper-key-name
  [address]
  (str address "-whisper"))

(defn can-save-user-password?
  [callback]
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
  (-> (.setInternetCredentials react-native-keychain
                               (string/lower-case server)
                               username
                               password
                               keychain-secure-hardware
                               keychain-restricted-availability)
      (.then callback)))

(defn get-credentials
  "Gets the credentials for a specified server from the Keychain"
  [server callback]
  (log/debug "[keychain] get-credentials")
  (-> (.getInternetCredentials react-native-keychain (string/lower-case server))
      (.then callback)))

(def auth-method-password "password")
(def auth-method-biometric "biometric")
(def auth-method-biometric-prepare "biometric-prepare")
(def auth-method-none "none")

(re-frame/reg-fx
 :keychain/get-auth-method
 (fn [[key-uid callback]]
   (can-save-user-password?
    (fn [can-save?]
      (if can-save?
        (get-credentials (str key-uid "-auth")
                         #(callback (if %
                                      (.-password ^js %)
                                      auth-method-none)))
        (callback nil))))))

(re-frame/reg-fx
 :keychain/get-user-password
 (fn [[key-uid callback]]
   (get-credentials key-uid #(if % (callback (security/mask-data (.-password ^js %))) (callback nil)))))

(re-frame/reg-fx
 :keychain/get-keycard-keys
 (fn [[key-uid callback]]
   (get-credentials
    key-uid
    (fn [^js encryption-key-data]
      (if encryption-key-data
        (get-credentials
         (whisper-key-name key-uid)
         (fn [^js whisper-key-data]
           (if whisper-key-data
             (callback [(.-password encryption-key-data)
                        (.-password whisper-key-data)])
             (callback nil))))
        (callback nil))))))

(re-frame/reg-fx
 :keychain/save-user-password
 (fn [[key-uid password]]
   (save-credentials
    key-uid
    key-uid
    (security/safe-unmask-data password)
    #(when-not %
       (log/error
        (str "Error while saving password."
             " "
             "The app will continue to work normally, "
             "but you will have to login again next time you launch it."))))))

(re-frame/reg-fx
 :keychain/save-auth-method
 (fn [[key-uid method]]
   (log/debug "[keychain] :keychain/save-auth-method"
              "key-uid"
              key-uid
              "method"
              method)
   (when-not (empty? key-uid) ; key-uid may be nil after restore from local pairing
     (save-credentials
      (str key-uid "-auth")
      key-uid
      method
      #(when-not %
         (log/error
          (str "Error while saving auth method."
               " "
               "The app will continue to work normally, "
               "but you will have to login again next time you launch it.")))))))

(re-frame/reg-fx
 :keychain/save-keycard-keys
 (fn [[key-uid encryption-public-key whisper-private-key]]
   (save-credentials
    key-uid
    key-uid
    encryption-public-key
    #(when-not %
       (log/error
        (str "Error while saving encryption-public-key"))))
   (save-credentials
    (whisper-key-name key-uid)
    key-uid
    whisper-private-key
    #(when-not %
       (log/error
        (str "Error while saving whisper-private-key"))))))

(re-frame/reg-fx
 :keychain/clear-user-password
 (fn [key-uid]
   (-> (.resetInternetCredentials react-native-keychain (string/lower-case key-uid))
       (.then #(when-not % (log/error (str "Error while clearing saved password.")))))))

(rf/defn get-auth-method
  [_ key-uid]
  {:keychain/get-auth-method
   [key-uid #(re-frame/dispatch [:multiaccounts.login/get-auth-method-success % key-uid])]})

(rf/defn get-user-password
  [_ key-uid]
  {:keychain/get-user-password
   [key-uid
    #(re-frame/dispatch
      [:multiaccounts.login.callback/get-user-password-success % key-uid])]})

(rf/defn get-keycard-keys
  [_ key-uid]
  {:keychain/get-keycard-keys
   [key-uid
    #(re-frame/dispatch
      [:multiaccounts.login.callback/get-keycard-keys-success key-uid %])]})

(rf/defn save-user-password
  [_ key-uid password]
  {:keychain/save-user-password [key-uid password]})

(rf/defn save-keycard-keys
  [_ key-uid encryption-public-key whisper-private-key]
  {:keychain/save-keycard-keys [key-uid
                                encryption-public-key
                                whisper-private-key]})
(rf/defn save-auth-method
  [{:keys [db]} key-uid method]
  {:db                        (assoc db :auth-method method)
   :keychain/save-auth-method [key-uid method]})
