(ns status-im.common.standard-authentication.events
  (:require
    [schema.core :as schema]
    [status-im.common.standard-authentication.enter-password.view :as enter-password]
    [status-im.common.standard-authentication.events-schema :as events-schema]
    [status-im.contexts.keycard.pin.view :as keycard.pin]
    [taoensso.timbre :as log]
    [utils.address]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(rf/reg-fx :effects.keycard/call-on-auth-success
 (fn [on-auth-success]
   (when on-auth-success (on-auth-success ""))))

(defn authorize
  [{:keys [db]} [{:keys [on-auth-success keycard-supported?] :as args}]]
  (let [key-uid  (get-in db [:profile/profile :key-uid])
        keycard? (get-in db [:profile/profile :keycard-pairing])]
    {:fx
     [(if keycard?
        (if keycard-supported?
          [:effects.keycard/call-on-auth-success on-auth-success]
          [:dispatch [:keycard/feature-unavailable-show]])
        [:effects.biometric/check-if-available
         {:key-uid    key-uid
          :on-success #(rf/dispatch [:standard-auth/authorize-with-biometric args])
          :on-fail    #(rf/dispatch [:standard-auth/authorize-with-password args])}])]}))

(schema/=> authorize events-schema/?authorize)
(rf/reg-event-fx :standard-auth/authorize authorize)

(defn authorize-with-biometric
  [_ [{:keys [on-auth-success on-auth-fail on-close] :as args}]]
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
             :on-success     (fn []
                               (rf/dispatch [:standard-auth/on-biometric-success on-auth-success])
                               (rf/dispatch [:standard-auth/close on-close]))
             :on-fail        (fn [err]
                               (rf/dispatch [:standard-auth/authorize-with-password
                                             args-with-biometric-btn])
                               (when on-auth-fail (on-auth-fail err))
                               (rf/dispatch [:standard-auth/on-biometric-fail err]))}]]]}))

(schema/=> authorize-with-biometric events-schema/?authorize-with-biometric)
(rf/reg-event-fx :standard-auth/authorize-with-biometric authorize-with-biometric)

(defn on-biometric-success
  [{:keys [db]} [on-auth-success]]
  (let [key-uid  (get-in db [:profile/profile :key-uid])
        keycard? (get-in db [:profile/profile :keycard-pairing])]
    {:fx [(if keycard?
            [:keychain/get-keycard-keys [key-uid on-auth-success]]
            [:keychain/get-user-password [key-uid on-auth-success]])
          [:dispatch [:standard-auth/set-success true]]
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
              (rf/dispatch [:standard-auth/set-success true])
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

(defn authorize-with-keycard
  [_ [{:keys [on-complete]}]]
  {:fx [[:dispatch
         [:show-bottom-sheet
          {:hide-on-background-press? false
           :on-close                  #(rf/dispatch [:standard-auth/reset-login-password])
           :content                   (fn []
                                        [keycard.pin/auth {:on-complete on-complete}])}]]]})

(rf/reg-event-fx :standard-auth/authorize-with-keycard authorize-with-keycard)

(defn authorize-with-password
  [_ [{:keys [on-close theme blur?] :as args}]]
  {:fx [[:dispatch [:standard-auth/reset-login-password]]
        [:dispatch
         [:show-bottom-sheet
          {:on-close #(rf/dispatch [:standard-auth/close on-close])
           :theme    theme
           :shell?   blur?
           :content  #(bottom-sheet-password-view args)}]]]})

(schema/=> authorize-with-password events-schema/?authorize-with-password)
(rf/reg-event-fx :standard-auth/authorize-with-password authorize-with-password)

(rf/reg-event-fx
 :standard-auth/reset-login-password
 (fn [{:keys [db]}]
   {:db (-> db
            (update :profile/login dissoc :password :error)
            (update :keycard dissoc :pin))}))

(rf/reg-fx
 :standard-auth/on-close
 (fn [{:keys [on-close success?]}]
   (when on-close
     (on-close success?))))

(rf/reg-event-fx
 :standard-auth/close
 (fn [{:keys [db]} [on-close]]
   {:db (assoc-in db [:profile/login :success?] false)
    :fx [[:dispatch [:standard-auth/reset-login-password]]
         [:standard-auth/on-close
          {:on-close on-close
           :success? (get-in db [:profile/login :success?])}]]}))

(rf/reg-event-fx
 :standard-auth/set-success
 (fn [{:keys [db]} [success?]]
   {:db (assoc-in db [:profile/login :success?] success?)}))
