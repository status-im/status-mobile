(ns status-im.common.standard-authentication.events
  (:require
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(rf/reg-event-fx
 :standard-auth/biometric-auth
 (fn [_ [{:keys [on-password-retrieved on-fail on-cancel]}]]
   {:fx [[:dispatch [:dismiss-keyboard]]
         [:dispatch
          [:biometric/authenticate
           {:prompt-message (i18n/label :t/biometric-auth-confirm-message)
            :on-cancel      on-cancel
            :on-success     #(rf/dispatch [:standard-auth/on-biometric-success
                                           on-password-retrieved])
            :on-fail        (fn [error]
                              (on-fail error)
                              (log/error (ex-message error)
                                         (-> error
                                             ex-data
                                             (assoc :code  (ex-cause error)
                                                    :event :standard-auth/biometric-auth))))}]]]}))

(rf/reg-event-fx :standard-auth/on-biometric-success
 (fn [{:keys [db]} [callback]]
   (let [key-uid (get-in db [:profile/profile :key-uid])]
     {:fx [[:keychain/get-user-password [key-uid callback]]]})))

(rf/reg-event-fx
 :standard-auth/reset-login-password
 (fn [{:keys [db]}]
   {:db (update db :profile/login dissoc :password :error)}))
