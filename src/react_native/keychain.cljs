(ns react-native.keychain
  (:require ["react-native-keychain" :as react-native-keychain]
            [clojure.string :as string]
            [taoensso.timbre :as log]))

;; ********************************************************************************
;; Storing / Retrieving a user password to/from Keychain
;; ********************************************************************************
;;
;; We are using set/get/reset internet credentials there because they are bound
;; to an address (`server`) property.

(defn enum-val
  [enum-name value-name]
  (get-in (js->clj ^js react-native-keychain) [enum-name value-name]))

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

;; Android only
(defn secure-hardware-available?
  [callback]
  (-> (.getSecurityLevel ^js react-native-keychain)
      (.then (fn [level] (callback (= level keychain-secure-hardware))))))

;; iOS only
(defn device-encrypted?
  [callback]
  (-> (.canImplyAuthentication
       ^js react-native-keychain
       (clj->js
        {:authenticationType
         (enum-val "ACCESS_CONTROL" "BIOMETRY_ANY_OR_DEVICE_PASSCODE")}))
      (.then callback)))

(defn save-credentials
  "Stores the credentials for the address to the Keychain"
  [server username password callback]
  (-> (.setInternetCredentials ^js react-native-keychain
                               (string/lower-case server)
                               username
                               password
                               keychain-secure-hardware
                               keychain-restricted-availability)
      (.then callback)))

(defn get-credentials
  "Gets the credentials for a specified server from the Keychain"
  [server callback]
  (-> (.getInternetCredentials ^js react-native-keychain (string/lower-case server))
      (.then callback)))

(defn reset-credentials
  [server]
  (-> (.resetInternetCredentials ^js react-native-keychain (string/lower-case server))
      (.then #(when-not % (log/error (str "Error while clearing saved password."))))))
