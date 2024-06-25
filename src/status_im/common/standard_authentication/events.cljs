(ns status-im.common.standard-authentication.events
  (:require
    [schema.core :as schema]
    [status-im.common.standard-authentication.enter-password.view :as enter-password]
    [status-im.common.standard-authentication.events-schema :as events-schema]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn authorize
  [{:keys [db]} [args]]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:fx [[:effects.biometric/check-if-available
           {:key-uid    key-uid
            :on-success #(rf/dispatch [:standard-auth/authorize-with-biometric args])
            :on-fail    #(rf/dispatch [:standard-auth/authorize-with-password args])}]]}))

(schema/=> authorize events-schema/?authorize)
(rf/reg-event-fx :standard-auth/authorize authorize)

(defn authorize-with-biometric
  [_ [{:keys [on-auth-success on-auth-fail] :as args}]]
  (let [args-with-biometric-btn
        (assoc args
               :on-press-biometric
               #(rf/dispatch [:standard-auth/authorize-with-biometric args]))]
    {:fx [[:dispatch [:dismiss-keyboard]]
          [:dispatch
           [:biometric/authenticate
            {:prompt-message (i18n/label :t/biometric-auth-confirm-message)
             :on-cancel      #(rf/dispatch [:standard-auth/authorize-with-password
                                            args-with-biometric-btn])
             :on-success     #(rf/dispatch [:standard-auth/on-biometric-success on-auth-success])
             :on-fail        (fn [err]
                               (rf/dispatch [:standard-auth/authorize-with-password
                                             args-with-biometric-btn])
                               (when on-auth-fail (on-auth-fail err))
                               (rf/dispatch [:standard-auth/on-biometric-fail err]))}]]]}))

(schema/=> authorize-with-biometric events-schema/?authorize-with-biometric)
(rf/reg-event-fx :standard-auth/authorize-with-biometric authorize-with-biometric)

(defn on-biometric-success
  [{:keys [db]} [on-auth-success]]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:fx [[:keychain/get-user-password [key-uid on-auth-success]]
          [:dispatch [:standard-auth/reset-login-password]]]}))

(schema/=> on-biometric-success events-schema/?on-biometric-success)
(rf/reg-event-fx :standard-auth/on-biometric-success on-biometric-success)

(defn on-biometric-fail
  [_ [error]]
  (log/error (ex-message error)
             (-> error
                 ex-data
                 (assoc :code  (ex-cause error)
                        :event :standard-auth/on-biometric-fail)))
  {:fx [[:dispatch [:standard-auth/reset-login-password]]
        [:dispatch [:biometric/show-message (ex-cause error)]]]})

(schema/=> on-biometric-fail events-schema/?on-biometrics-fail)
(rf/reg-event-fx :standard-auth/on-biometric-fail on-biometric-fail)

(defn- bottom-sheet-password-view
  [{:keys [on-press-biometric on-auth-success auth-button-icon-left auth-button-label]}]
  (fn []
    (let [has-partially-operable-accounts? (rf/sub [:wallet/has-partially-operable-accounts?])
          handle-password-success
          (fn [password]
            (let [sha3-pwd                 (security/hash-masked-password password)
                  on-auth-success-callback #(on-auth-success sha3-pwd)]
              (rf/dispatch [:standard-auth/reset-login-password])
              (if has-partially-operable-accounts?
                (rf/dispatch [:wallet/make-partially-operable-accounts-fully-operable
                              {:password   sha3-pwd
                               :on-success on-auth-success-callback
                               :on-error   on-auth-success-callback}])
                (on-auth-success-callback))))]
      [enter-password/view
       {:on-enter-password   handle-password-success
        :on-press-biometrics on-press-biometric
        :button-icon-left    auth-button-icon-left
        :button-label        auth-button-label}])))

(defn authorize-with-password
  [_ [{:keys [on-close theme blur?] :as args}]]
  {:fx [[:dispatch [:standard-auth/reset-login-password]]
        [:dispatch
         [:show-bottom-sheet
          {:on-close (fn []
                       (rf/dispatch [:standard-auth/reset-login-password])
                       (when on-close
                         (on-close)))
           :theme    theme
           :shell?   blur?
           :content  #(bottom-sheet-password-view args)}]]]})

(schema/=> authorize-with-password events-schema/?authorize-with-password)
(rf/reg-event-fx :standard-auth/authorize-with-password authorize-with-password)

(rf/reg-event-fx
 :standard-auth/reset-login-password
 (fn [{:keys [db]}]
   {:db (update db :profile/login dissoc :password :error)}))
