(ns status-im.common.standard-authentication.events
  (:require
    [status-im.common.standard-authentication.enter-password.view :as enter-password]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

;; REMOVE
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

(rf/reg-event-fx
 :standard-auth/authorize
 (fn [{:keys [db]} [args]]
   (let [key-uid (get-in db [:profile/profile :key-uid])]
     {:fx [[:biometric/check-if-available
            {:key-uid    key-uid
             :on-success [:standard-auth/authorize-with-biometric args]
             :on-fail    [:standard-auth/authorize-with-password args]}]]})))

(rf/reg-event-fx
 :standard-auth/authorize-with-biometric
 (fn [_ [{:keys [on-auth-success on-auth-fail] :as args}]]
   (let [args-with-biometric-btn
         (assoc args
                :on-press-biometric
                #(rf/dispatch [:standard-auth/authorize-with-biometric args]))]
     {:fx [[:dispatch [:dismiss-keyboard]]
           [:dispatch
            [:biometric/authenticate
             {:prompt-message (i18n/label :t/biometric-auth-confirm-message)
              :on-cancel      [:standard-auth/authorize-with-password args-with-biometric-btn]
              :on-success     [:standard-auth/on-biometric-success on-auth-success]
              :on-fail        [:standard-auth/on-biometric-fail on-auth-fail]}]]]})))

(rf/reg-event-fx
 :standard-auth/on-biometric-success
 (fn [{:keys [db]} [on-auth-success]]
   (let [key-uid (get-in db [:profile/profile :key-uid])]
     {:fx [[:keychain/get-user-password [key-uid on-auth-success]]]})))


(rf/reg-event-fx
 :standard-auth/on-biometric-fail
 (fn [_ [on-auth-fail error]]
   (when on-auth-fail
     (on-auth-fail error))
   (log/error (ex-message error)
              (-> error
                  ex-data
                  (assoc :code  (ex-cause error)
                         :event :standard-auth/biometric-auth)))
   {:fx [[:dispatch [:biometric/show-message (ex-cause error)]]]}))

(defn- bottom-sheet-password-view
  [{:keys [on-press-biometric on-auth-success auth-button-icon-left auth-button-label]}]
  (fn []
    (let [handle-password-success (fn [password]
                                    (-> password security/hash-masked-password on-auth-success))]
      [enter-password/view
       {:on-enter-password   handle-password-success
        :on-press-biometrics on-press-biometric
        :button-icon-left    auth-button-icon-left
        :button-label        auth-button-label}])))

(rf/reg-event-fx
 :standard-auth/authorize-with-password
 (fn [_ [{:keys [on-close theme blur?] :as args}]]
   {:fx [[:dispatch [:standard-auth/reset-login-password]]
         [:dispatch
          [:show-bottom-sheet
           {:on-close on-close
            :theme    theme
            :shell?   blur?
            :content  #(bottom-sheet-password-view args)}]]]}))

(rf/reg-event-fx
 :standard-auth/reset-login-password
 (fn [{:keys [db]}]
   {:db (update db :profile/login dissoc :password :error)}))
