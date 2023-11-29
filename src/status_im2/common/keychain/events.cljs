(ns status-im2.common.keychain.events
  (:require
    [native-module.core :as native-module]
    [oops.core :as oops]
    [re-frame.core :as re-frame]
    [react-native.keychain :as keychain]
    [react-native.platform :as platform]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]
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

;; These helpers check if the device is okay to use for password storage
;; They resolve callback with `true` if the check is passed, with `false` otherwise.
;; Android only
(defn- device-not-rooted?
  [callback]
  (native-module/rooted-device? (fn [rooted?] (callback (not rooted?)))))

(defn can-save-user-password?
  [callback]
  (log/debug "[keychain] can-save-user-password?")
  (cond
    platform/ios?
    (check-conditions callback keychain/device-encrypted?)

    platform/android?
    (check-conditions callback keychain/secure-hardware-available? device-not-rooted?)

    :else
    (callback false)))

(def auth-method-biometric "biometric")
(def auth-method-biometric-prepare "biometric-prepare")
(def auth-method-none "none")

(defn save-auth-method!
  [key-uid method]
  (keychain/save-credentials
   (str key-uid "-auth")
   key-uid
   method
   #(when-not %
      (log/error
       (str "Error while saving auth method."
            " "
            "The app will continue to work normally, "
            "but you will have to login again next time you launch it.")))))

(re-frame/reg-fx
 :keychain/save-auth-method
 (fn [[key-uid method]]
   (when-not (empty? key-uid) ; key-uid may be nil after restore from local pairing
     (save-auth-method! key-uid method))))

(rf/defn save-auth-method
  [{:keys [db]} key-uid method]
  {:db                        (assoc db :auth-method method)
   :keychain/save-auth-method [key-uid method]})

(re-frame/reg-fx
 :keychain/get-auth-method
 (fn [[key-uid callback]]
   (can-save-user-password?
    (fn [can-save?]
      (if can-save?
        (keychain/get-credentials
         (str key-uid "-auth")
         #(callback (if % (oops/oget % "password") auth-method-none)))
        (callback nil))))))

(defn save-user-password!
  [key-uid password]
  (keychain/save-credentials key-uid key-uid (security/safe-unmask-data password) #()))

(defn get-user-password!
  [key-uid callback]
  (keychain/get-credentials key-uid
                            #(callback (when %
                                         (-> %
                                             (oops/oget "password")
                                             (security/mask-data))))))

(re-frame/reg-fx
 :keychain/get-user-password
 (fn [[key-uid callback]]
   (get-user-password! key-uid callback)))

(rf/defn get-user-password
  [_ key-uid callback]
  {:keychain/get-user-password [key-uid callback]})

(defn- password-migration-key-name
  [key-uid]
  (str key-uid "-password-migration"))

(defn save-password-migration!
  [key-uid]
  (keychain/save-credentials
   (password-migration-key-name key-uid)
   key-uid
   ;; NOTE: using the key-id as the password, but we don't really care about the
   ;; value, we only care that it's there
   key-uid
   #(when-not %
      (log/error
       (str "Error while setting up keychain migration")))))

(defn get-password-migration!
  [key-uid callback]
  (keychain/get-credentials
   (password-migration-key-name key-uid)
   (comp callback boolean)))

(re-frame/reg-fx
 :keychain/clear-user-password
 (fn [key-uid]
   (keychain/reset-credentials (password-migration-key-name key-uid))
   (keychain/reset-credentials key-uid)))

(re-frame/reg-fx
 :keychain/save-password-and-auth-method
 (fn [{:keys [key-uid masked-password on-success on-error]}]
   (-> (save-user-password! key-uid masked-password)
       (.then #(save-auth-method! key-uid auth-method-biometric))
       (.then #(save-password-migration! key-uid))
       (.then #(when on-success (on-success)))
       (.catch #(when on-error (on-error %))))))

;; NOTE: migrating the plaintext password in the keychain
;; with the hashed one. Added due to the sync onboarding
;; flow, where the password arrives already hashed.
(re-frame/reg-fx
 :keychain/password-hash-migration
 (fn [{:keys [key-uid callback]}]
   (-> (get-password-migration! key-uid identity)
       (.then (fn [migrated?]
                (if migrated?
                  (callback)
                  (-> (get-user-password! key-uid identity)
                      (.then #(security/hash-masked-password %))
                      (.then #(save-user-password! key-uid %))
                      (.then #(save-password-migration! key-uid))
                      (.then #(callback))
                      (.catch #(log/error "Failed to migrate the keychain password for " key-uid
                                          "\nError: "                                    %)))))))))
