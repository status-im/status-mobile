(ns status-im.common.standard-authentication.events
  (:require
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(rf/reg-event-fx :standard-auth/on-biometric-success
 (fn [{:keys [db]} [callback]]
   (let [key-uid (get-in db [:profile/profile :key-uid])]
     {:fx [[:keychain/get-user-password [key-uid callback]]]})))

(rf/reg-event-fx
 :standard-auth/reset-login-password
 (fn [{:keys [db]}]
   {:db (update db :profile/login dissoc :password :error)}))

(rf/reg-event-fx
 :standard-auth/update-password
 (fn [{:keys [db]} [value]]
   {:db (-> db
            (assoc-in [:profile/login :password] (security/mask-data value))
            (assoc-in [:profile/login :error] ""))}))
