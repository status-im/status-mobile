(ns status-im2.contexts.profile.login.events
  (:require [utils.re-frame :as rf]
            [status-im.ethereum.core :as ethereum]
            [utils.security.core :as security]
            [re-frame.core :as re-frame]
            [native-module.core :as native-module]
            [status-im2.navigation.events :as navigation]
            [status-im2.common.keychain.events :as keychain]
            [status-im2.common.biometric.events :as biometric]
            [status-im2.contexts.profile.config :as profile.config]))

(re-frame/reg-fx
 ::login
 (fn [[key-uid hashed-password]]
   ;;"node.login" signal will be triggered as a callback
   (native-module/login-account
    (assoc (profile.config/login) :keyUid key-uid :password hashed-password))))

(rf/defn login
  {:events [:profile.login/login]}
  [{:keys [db]}]
  (let [{:keys [key-uid password]} (:profile/login db)]
    {:db     (assoc-in db [:profile/login :processing] true)
     ::login [key-uid (ethereum/sha3 (security/safe-unmask-data password))]}))

(rf/defn login-local-paired-user
  {:events [:profile.login/local-paired-user]}
  [{:keys [db]}]
  (let [{:keys [key-uid password]} (get-in db [:syncing :profile])]
    {::login [key-uid password]}))

(rf/defn login-with-biometric-if-available
  {:events [:profile.login/login-with-biometric-if-available]}
  [_ key-uid]
  {:keychain/get-auth-method [key-uid
                              #(rf/dispatch [:profile.login/get-auth-method-success % key-uid])]})

(rf/defn get-auth-method-success
  {:events [:profile.login/get-auth-method-success]}
  [{:keys [db]} auth-method]
  (merge {:db (assoc db :auth-method auth-method)}
         (when (= auth-method keychain/auth-method-biometric)
           {:biometric/authenticate
            {:on-success #(rf/dispatch [:profile.login/biometric-success])
             :on-faile   #(rf/dispatch [:profile.login/biometric-auth-fail])}})))

(rf/defn biometric-auth-success
  {:events [:profile.login/biometric-success]}
  [{:keys [db] :as cofx}]
  (let [key-uid (get-in db [:profile/login :key-uid])]
    (keychain/get-user-password cofx
                                key-uid
                                #(rf/dispatch [:profile.login/get-user-password-success %]))))

;; result of :keychain/get-auth-method above
(rf/defn get-user-password-success
  {:events [:profile.login/get-user-password-success]}
  [{:keys [db] :as cofx} password]
  (when password
    (rf/merge
     cofx
     {:db (assoc-in db [:profile/login :password] password)}
     (navigation/init-root :progress)
     (login))))

(rf/defn biometric-auth-fail
  {:events [:profile.login/biometric-auth-fail]}
  [{:keys [db] :as cofx} code]
  (rf/merge cofx
            (navigation/init-root :profiles)
            (biometric/show-message code)))
