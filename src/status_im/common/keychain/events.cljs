(ns status-im.common.keychain.events
  (:require
    [native-module.core :as native-module]
    [oops.core :as oops]
    [re-frame.core :as re-frame]
    [react-native.keychain :as keychain]
    [react-native.platform :as platform]
    [status-im.common.log :as log]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn- whisper-key-name
  [address]
  (str address "-whisper"))

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

;; These helpers check if the device is okay to use for password storage
;; They resolve callback with `true` if the check is passed, with `false` otherwise.
;; Android only
(defn- device-not-rooted?
  [callback]
  (native-module/rooted-device? (fn [rooted?] (callback (not rooted?)))))

(defn can-save-user-password?
  [callback]
  (log/log-debug "[keychain] can-save-user-password?")
  (cond
    platform/ios?
    (check-conditions callback keychain/device-encrypted?)

    platform/android?
    (check-conditions callback keychain/secure-hardware-available? device-not-rooted?)

    :else
    (callback false)))

(def auth-method-biometric "biometric")
(def auth-method-none "none")

(defn auth-credentials
  [key-uid]
  (str key-uid "-auth"))

(defn save-auth-method!
  [key-uid method]
  (-> (keychain/save-credentials
       (auth-credentials key-uid)
       key-uid
       method)
      (.catch (fn [err]
                (log/log-error "Failed to save auth method in the keychain"
                               {:error       err
                                :key-uid     key-uid
                                :auth-method method})))))

(re-frame/reg-fx
 :keychain/save-auth-method
 (fn [[key-uid method]]
   (when-not (empty? key-uid) ; key-uid may be nil after restore from local pairing
     (save-auth-method! key-uid method))))

(defn get-auth-method!
  [key-uid]
  (-> (auth-credentials key-uid)
      (keychain/get-credentials)
      (.then (fn [value]
               (if value
                 (oops/oget value "password")
                 auth-method-none)))
      (.catch (constantly auth-method-none))))

(defn save-user-password!
  [key-uid password]
  (keychain/save-credentials key-uid key-uid (security/safe-unmask-data password)))

(defn get-user-password!
  [key-uid callback]
  (keychain/get-credentials key-uid
                            (fn [value]
                              (some-> value
                                      (oops/oget "password")
                                      (security/mask-data)
                                      (callback)))))

(re-frame/reg-fx
 :keychain/get-user-password
 (fn [[key-uid callback]]
   (get-user-password! key-uid callback)))

(rf/defn get-user-password
  [_ [key-uid callback]]
  {:keychain/get-user-password [key-uid callback]})

(defn- password-migration-key-name
  [key-uid]
  (str key-uid "-password-migration"))

(defn save-password-migration!
  [key-uid]
  (-> (keychain/save-credentials
       (password-migration-key-name key-uid)
       key-uid
       ;; NOTE: using the key-uid as the password, but we don't really care about the
       ;; value, we only care that it's there
       key-uid)
      (.catch (fn [error]
                (log/log-error "Failed to get the keychain password migration flag"
                               {:error   error
                                :key-uid key-uid})))))

(defn get-password-migration!
  [key-uid callback]
  (keychain/get-credentials
   (password-migration-key-name key-uid)
   (comp callback boolean)))

(re-frame/reg-fx
 :keychain/clear-user-password
 (fn [key-uid]
   (keychain/reset-credentials (password-migration-key-name key-uid))
   (keychain/reset-credentials (auth-credentials key-uid))
   (keychain/reset-credentials key-uid)))

(re-frame/reg-fx
 :keychain/clear-keycard-keys
 (fn [key-uid]
   (keychain/reset-credentials (auth-credentials key-uid))
   (keychain/reset-credentials (whisper-key-name key-uid))
   (keychain/reset-credentials key-uid)))

(re-frame/reg-fx
 :keychain/save-password-and-auth-method
 (fn [{:keys [key-uid masked-password on-success on-error]}]
   (-> (save-user-password! key-uid masked-password)
       (.then #(save-auth-method! key-uid auth-method-biometric))
       (.then #(save-password-migration! key-uid))
       (.then #(when on-success (on-success)))
       (.catch #(when on-error (on-error %))))))

(re-frame/reg-event-fx
 :keychain/save-password-and-auth-method
 (fn [_ [opts]]
   {:keychain/save-password-and-auth-method opts}))

;; NOTE: migrating the plaintext password in the keychain
;; with the hashed one. Added due to the sync onboarding
;; flow, where the password arrives already hashed.
(re-frame/reg-fx
 :keychain/password-hash-migration
 (fn [{:keys [key-uid callback]
       :or   {callback identity}}]
   (keychain/get-credentials
    (whisper-key-name key-uid)
    (fn [whisper-key-data]
      (if whisper-key-data
        (callback) ;; we don't need to migrate keycard password
        (-> (get-password-migration! key-uid identity)
            (.then (fn [migrated?]
                     (if migrated?
                       (callback)
                       (-> (get-user-password! key-uid identity)
                           (.then security/hash-masked-password)
                           (.then #(save-user-password! key-uid %))
                           (.then #(save-password-migration! key-uid))
                           (.then callback)))))
            (.catch (fn [err]
                      (log/log-error "Failed to migrate the keychain password"
                                     {:error   err
                                      :key-uid key-uid
                                      :event   :keychain/password-hash-migration})))))))))

(re-frame/reg-fx
 :effects.keychain/save-keycard-keys
 (fn [keycard-keys]
   (let [{:keys [key-uid encryption-public-key whisper-private-key]}
         (security/safe-unmask-data keycard-keys)]
     (keychain/save-credentials
      key-uid
      key-uid
      encryption-public-key
      #(when-not % (log/log-error "Error while saving encryption-public-key")))
     (keychain/save-credentials
      (whisper-key-name key-uid)
      key-uid
      whisper-private-key
      #(when-not % (log/log-error "Error while saving whisper-private-key"))))))

(re-frame/reg-event-fx :keychain/save-keycard-keys-and-auth-method
 (fn [_ [keycard-keys]]
   (let [{:keys [key-uid]} (security/safe-unmask-data keycard-keys)]
     {:effects.keychain/save-keycard-keys keycard-keys
      :keychain/save-auth-method          [key-uid auth-method-biometric]})))

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
