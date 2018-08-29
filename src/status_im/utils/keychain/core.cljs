(ns status-im.utils.keychain.core
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.react-native.js-dependencies :as rn]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.platform :as platform]))

(def key-bytes 64)
(def username "status-im.encryptionkey")

(defn- bytes->js-array [b]
  (.from js/Array b))

(defn- string->js-array [s]
  (.parse js/JSON (.-password s)))

;; ********************************************************************************
;; Storing / Retrieving a user password to/from Keychain
;; ********************************************************************************
;;
;; We are using set/get/reset internet credentials there because they are bound
;; to an address (`server`) property.


(defn enum-val [enum-name value-name]
  (get-in (js->clj rn/keychain) [enum-name value-name]))

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
  {:accessible    (enum-val "ACCESSIBLE"     "WHEN_PASSCODE_SET_THIS_DEVICE_ONLY")})

;; Stores the password for the address to the Keychain
(defn save-user-password [address password callback]
  (if-not platform/ios?
    (callback true) ;; no-op on Androids (for now)
    (-> (.setInternetCredentials rn/keychain address address password
                                 (clj->js keychain-restricted-availability))
        (.then callback))))

(defn handle-callback [callback result]
  (if result
    (callback (.-password result))
    (callback nil)))

;; Gets the password for a specified address from the Keychain
(defn get-user-password [address callback]
  (if-not platform/ios?
    (callback) ;; no-op on Androids (for now)
    (-> (.getInternetCredentials rn/keychain address)
        (.then (partial handle-callback callback)))))

;; Clears the password for a specified address from the Keychain
;; (example of usage is logout or signing in w/o "save-password")
(defn clear-user-password [address callback]
  (if-not platform/ios?
    (callback true)
    (-> (.resetInternetCredentials rn/keychain address)
        (.then callback))))

;; Resolves to `false` if the device doesn't have neither a passcode nor a biometry auth.
(defn can-save-user-password? [callback]
  (if-not platform/ios?
    (callback false)
    (-> (.canImplyAuthentication
         rn/keychain
         (clj->js
          {:authenticationType
           (enum-val "ACCESS_CONTROL" "BIOMETRY_ANY_OR_DEVICE_PASSCODE")}))
        (.then callback))))

;; ********************************************************************************
;; Storing / Retrieving the realm encryption key to/from the Keychain
;; ********************************************************************************


;; Smoke test key to make sure is ok, we noticed some non-random keys on
;; some IOS devices. We check naively that there are no more than key-bytes/2
;; identical characters.
(defn validate
  [encryption-key]
  (cond
    (or (not encryption-key)
        (not= (.-length encryption-key) key-bytes))
    (.reject js/Promise {:error :invalid-key
                         :key    encryption-key})

    (>= (/ key-bytes 2)
        (count (keys (group-by identity encryption-key))))
    (.reject js/Promise {:error :weak-key
                         :key   encryption-key})

    :else encryption-key))

(defn store [encryption-key]
  (log/debug "storing encryption key")
  (-> (.setGenericPassword
       rn/keychain
       username
       (.stringify js/JSON encryption-key))
      (.then (constantly encryption-key))))

(defn create []
  (log/debug "no key exists, creating...")
  (.. (rn/secure-random key-bytes)
      (then bytes->js-array)))

(defn handle-not-found []
  (.. (create)
      (then validate)
      (then store)))

(def handle-found
  (comp validate
        string->js-array))

(defn get-encryption-key []
  (log/debug "initializing realm encryption key...")
  (.. (.getGenericPassword rn/keychain)
      (then
       (fn [res]
         (if res
           (handle-found res)
           (handle-not-found))))))

(defn safe-get-encryption-key
  "Return encryption key or empty string in case invalid/empty"
  []
  (log/debug "initializing realm encryption key...")
  (.. (get-encryption-key)
      (catch (fn [{:keys [_ key]}]
               (log/warn "key is invalid, continuing")
               (or key "")))))

(defn reset []
  (log/debug "resetting key...")
  (.resetGenericPassword rn/keychain))

(defn set-username []
  (when platform/desktop? (.setUsername rn/keychain username)))

;;;; Effects

(re-frame/reg-fx
 :save-user-password
 (fn [[address password]]
   (save-user-password
    address
    password
    #(when-not %
       (log/error
        (str "Error while saving password."
             " "
             "The app will continue to work normally, "
             "but you will have to login again next time you launch it."))))))

(re-frame/reg-fx
 :get-user-password
 (fn [[address callback]]
   (get-user-password address callback)))

(re-frame/reg-fx
 :clear-user-password
 (fn [address]
   (clear-user-password
    address
    #(when-not %
       (log/error (str "Error while clearing saved password."))))))

(re-frame/reg-fx
 :can-save-user-password?
 (fn [_]
   (can-save-user-password? #(re-frame/dispatch [:callback/can-save-user-password? %]))))

(handlers/register-handler-fx
 :callback/can-save-user-password?
 (fn [{:keys [db]} [_ can-save-user-password?]]
   {:db (assoc-in db [:accounts/login :can-save-password?] can-save-user-password?)}))
