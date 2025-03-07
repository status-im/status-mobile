(ns status-im.common.standard-authentication.events
  (:require
    [schema.core :as schema]
    [status-im.common.keychain.events :as keychain]
    [status-im.common.standard-authentication.enter-password.view :as enter-password]
    [status-im.common.standard-authentication.events-schema :as events-schema]
    status-im.common.standard-authentication.keycard-events
    [status-im.common.standard-authentication.utils :as utils]
    [status-im.contexts.wallet.data-store :as data-store]
    [taoensso.timbre :as log]
    [utils.address]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn authorize-and-sign
  [{:keys [db]} [{:keys [sign-payload on-sign-success on-sign-error] :as args}]]
  (let [keypairs         (get-in db [:wallet :keypairs])
        keycard-keypair? (->> sign-payload
                              first
                              :address
                              (utils/keycard-account? keypairs))]
    {:fx [(cond
            ;; NOTE: signing with both the keycard keypair and a non-keycard keypair (on-device)
            ;; is not yet supported, since it would require both scanning the keycard and entering
            ;; the password in the same flow, and should be fixed from the user-flow perspective
            (and (utils/keycard-profile? db)
                 (utils/payloads-for-different-keypairs? keypairs sign-payload))
            [:dispatch
             [:keycard/feature-unavailable-show
              {:theme :dark
               :feature-name
               :standard-auth/multiple-keypair-signing}]]

            keycard-keypair? [:dispatch
                              [:standard-auth/authorize-with-keycard
                               {:on-complete (fn [pin]
                                               (rf/dispatch
                                                [:keycard/connect-and-sign-payloads
                                                 {:keycard-pin pin
                                                  :payloads    sign-payload
                                                  :on-success  (fn [signatures]
                                                                 (rf/dispatch [:hide-bottom-sheet])
                                                                 (on-sign-success signatures))
                                                  :on-failure  on-sign-error}]))}]]



            :else
            [:dispatch
             [:standard-auth/authorize
              (assoc args
                     :on-auth-success
                     (fn [masked-password]
                       (rf/dispatch [:standard-auth/sign-on-device
                                     {:masked-password masked-password
                                      :sign-payload    sign-payload
                                      :on-success      on-sign-success
                                      :on-error        on-sign-error}])))]])]}))

(schema/=> authorize-and-sign events-schema/?authorize-and-sign)
(rf/reg-event-fx :standard-auth/authorize-and-sign authorize-and-sign)

(defn sign-on-device
  [_ [{:keys [masked-password sign-payload on-success on-error]}]]
  {:fx [[:effects.wallet/sign-payloads
         {:payloads   sign-payload
          :password   masked-password
          :on-success on-success
          :on-error   on-error}]]})

(rf/reg-event-fx :standard-auth/sign-on-device sign-on-device)

(defn authorize
  [{:keys [db]} [args]]
  {:fx [(cond
          (utils/biometrics-enabled? db) [:dispatch [:standard-auth/authorize-with-biometric args]]
          (utils/keycard-profile? db)    [:dispatch [:standard-auth/authorize-with-keycard-key args]]
          :else                          [:dispatch [:standard-auth/authorize-with-password args]])]})

(schema/=> authorize events-schema/?authorize)
(rf/reg-event-fx :standard-auth/authorize authorize)

(defn authorize-with-biometric
  [_ [{:keys [on-auth-success on-auth-fail] :as args}]]
  {:fx [[:dispatch [:dismiss-keyboard]]
        [:dispatch
         [:biometric/authenticate
          {:prompt-message (i18n/label :t/biometric-auth-confirm-message)
           :on-cancel      #(rf/dispatch [:standard-auth/fallback-for-biometrics args])
           :on-success     (fn []
                             (rf/dispatch [:standard-auth/on-biometric-success on-auth-success]))
           :on-fail        (fn [err]
                             (rf/dispatch [:standard-auth/fallback-for-biometrics args])
                             (when on-auth-fail (on-auth-fail err))
                             (rf/dispatch [:standard-auth/on-biometric-fail err]))}]]]})

(defn fallback-for-biometrics
  [{:keys [db]} [args]]
  {:fx [(if (utils/keycard-profile? db)
          [:dispatch [:standard-auth/authorize-with-keycard-key args]]
          [:dispatch [:standard-auth/authorize-with-password args]])]})

(rf/reg-event-fx :standard-auth/fallback-for-biometrics fallback-for-biometrics)

(schema/=> authorize-with-biometric events-schema/?authorize-with-biometric)
(rf/reg-event-fx :standard-auth/authorize-with-biometric authorize-with-biometric)

(defn get-keychain-key
  [{:keys [db]} [on-done]]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:fx [(if (utils/keycard-profile? db)
            [:keychain/get-keycard-keys [key-uid on-done]]
            [:keychain/get-user-password [key-uid on-done]])]}))

(rf/reg-event-fx :standard-auth/get-keychain-key get-keychain-key)

(defn on-biometric-success
  [_ [on-auth-success]]
  {:fx [[:dispatch
         [:standard-auth/get-keychain-key
          (fn [masked-key]
            (rf/dispatch [:standard-auth/finish-auth
                          {:on-auth-success on-auth-success
                           :masked-password masked-key}]))]]]})

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
  [{:keys [on-auth-success auth-button-icon-left auth-button-label hide-biometrics-button?] :as args}]
  (fn []
    (let [auth-method        (rf/sub [:auth-method])
          biometric-enabled? (= auth-method keychain/auth-method-biometric)]
      [enter-password/view
       {:on-enter-password   #(rf/dispatch [:standard-auth/finish-auth
                                            {:masked-password (security/hash-masked-password %)
                                             :on-auth-success on-auth-success}])
        :on-press-biometrics (when (and (not hide-biometrics-button?) biometric-enabled?)
                               #(rf/dispatch [:standard-auth/authorize-with-biometric args]))
        :button-icon-left    auth-button-icon-left
        :button-label        auth-button-label}])))

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

(rf/reg-fx
 :effects.standard-auth/on-auth-success
 (fn [on-auth-success]
   (when on-auth-success
     (on-auth-success))))

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

(defn- finish-auth
  [{:keys [db]} [{:keys [masked-password on-auth-success]}]]
  (let [on-auth-success-callback         #(on-auth-success masked-password)
        has-partially-operable-accounts? (-> (get-in db [:wallet :accounts])
                                             data-store/partially-operable-accounts?)
        keycard-profile?                 (utils/keycard-profile? db)]
    {:fx [[:dispatch [:standard-auth/set-success true]]
          [:dispatch [:standard-auth/reset-login-password]]
          (if (and has-partially-operable-accounts? (not keycard-profile?))
            [:dispatch
             [:wallet/make-partially-operable-accounts-fully-operable
              {:password   masked-password
               :on-success on-auth-success-callback
               :on-error   on-auth-success-callback}]]
            [:effects.standard-auth/on-auth-success on-auth-success-callback])]}))

(rf/reg-event-fx :standard-auth/finish-auth finish-auth)

(rf/reg-fx
 :effects.standard-auth/on-auth-success
 (fn [on-auth-success]
   (when on-auth-success
     (on-auth-success))))
