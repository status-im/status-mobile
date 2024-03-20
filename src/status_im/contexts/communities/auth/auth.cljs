(ns status-im.contexts.communities.auth.auth
  (:require [status-im.common.keychain.events :as keychain]
            [utils.re-frame :as rf]
            [utils.security.core :as security]))

(rf/reg-event-fx
 :communities/login-with-biometric-if-available
 (fn [_ [{:keys [key-uid on-fail on-success]}]]
   {:fx [[:effects.biometric/check-if-available
          {:key-uid    key-uid
           :on-success (fn [auth-method]
                         (rf/dispatch
                          [:communities/check-biometric-success
                           key-uid auth-method on-success on-fail]))
           :on-fail    on-fail}]]}))

(rf/reg-event-fx
 :communities/check-biometric-success
 (fn [{:keys [db]} [key-uid auth-method on-success on-fail]]
   {:db (assoc db :auth-method auth-method)
    :fx [(when (= auth-method keychain/auth-method-biometric)
           [:keychain/password-hash-migration
            {:key-uid  key-uid
             :callback (fn []
                         (rf/dispatch
                          [:biometric/authenticate
                           {:on-success #(rf/dispatch
                                          [:communities/biometric-success on-success key-uid])
                            :on-fail    on-fail}]))}])]}))

(rf/reg-event-fx
 :communities/get-user-password-success
 (fn [_ [password on-success]]
   (when password
     {:fx [(on-success (security/safe-unmask-data password))]})))

(rf/reg-event-fx
 :communities/biometric-success
 (fn [_ [on-success key-uid]]
   {:keychain/get-user-password [key-uid
                                 #(rf/dispatch [:communities/get-user-password-success %
                                                on-success])]}))
