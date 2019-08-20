(ns status-im.utils.keychain.core
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.react-native.js-dependencies :as rn]
            [status-im.utils.platform :as platform]
            [status-im.utils.security :as security]
            [status-im.native-module.core :as status]))

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
  (get-in (js->clj (rn/keychain)) [enum-name value-name]))

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
  {:accessible (enum-val "ACCESSIBLE" "WHEN_PASSCODE_SET_THIS_DEVICE_ONLY")})

(def keychain-secure-hardware
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
  (-> (.getSecurityLevel (rn/keychain))
      (.then (fn [level] (callback (= level keychain-secure-hardware))))))

;; iOS only
(defn- device-encrypted? [callback]
  (-> (.canImplyAuthentication
       (rn/keychain)
       (clj->js
        {:authenticationType
         (enum-val "ACCESS_CONTROL" "BIOMETRY_ANY_OR_DEVICE_PASSCODE")}))
      (.then callback)))

;; Stores the password for the address to the Keychain
(defn save-user-password [address password callback]
  (-> (.setInternetCredentials (rn/keychain) address address password keychain-secure-hardware (clj->js keychain-restricted-availability))
      (.then callback)))

(defn handle-callback [callback result]
  (if result
    (callback (security/mask-data (.-password result)))
    (callback nil)))

;; Gets the password for a specified address from the Keychain
(defn get-user-password [address callback]
  (if (or platform/ios? platform/android?)
    (-> (.getInternetCredentials (rn/keychain) address)
        (.then (partial handle-callback callback)))
    (callback))) ;; no-op for Desktop

;; Clears the password for a specified address from the Keychain
;; (example of usage is logout or signing in w/o "save-password")
(defn clear-user-password [address callback]
  (if (or platform/ios? platform/android?)
    (-> (.resetInternetCredentials (rn/keychain) address)
        (.then callback))
    (callback true))) ;; no-op for Desktop

;; Resolves to `false` if the device doesn't have neither a passcode nor a biometry auth.
(defn can-save-user-password? [callback]
  (cond
    platform/ios? (check-conditions callback
                                    device-encrypted?)

    platform/android?  (check-conditions
                        callback
                        secure-hardware-available?
                        device-not-rooted?)

    :else (callback false)))

;;;; Effects

(re-frame/reg-fx
 :keychain/save-user-password
 (fn [[address password]]
   (save-user-password
    address
    (security/safe-unmask-data password)
    #(when-not %
       (log/error
        (str "Error while saving password."
             " "
             "The app will continue to work normally, "
             "but you will have to login again next time you launch it."))))))

(re-frame/reg-fx
 :keychain/get-user-password
 (fn [[address callback]]
   (get-user-password address callback)))

(re-frame/reg-fx
 :keychain/clear-user-password
 (fn [address]
   (clear-user-password
    address
    #(when-not %
       (log/error (str "Error while clearing saved password."))))))

(re-frame/reg-fx
 :keychain/can-save-user-password?
 (fn [_]
   (can-save-user-password? #(re-frame/dispatch [:keychain.callback/can-save-user-password?-success %]))))
