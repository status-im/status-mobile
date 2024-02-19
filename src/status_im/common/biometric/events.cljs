(ns status-im.common.biometric.events
  (:require
    [react-native.biometrics :as biometrics]
    [schema.core :as schema]
    [status-im.common.biometric.events-schema :as events-schema]
    [status-im.common.biometric.utils :as utils]
    [status-im.common.keychain.events :as keychain]
    [status-im.constants :as constants]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(rf/reg-fx
 :biometric/get-supported-type
 (fn []
   ;;NOTE: if we can't save user password, we can't use biometric
   (keychain/can-save-user-password?
    (fn [can-save?]
      (when (and can-save? (not utils/android-device-blacklisted?))
        (-> (biometrics/get-supported-type)
            (.then (fn [type]
                     (rf/dispatch [:biometric/set-supported-type type])))))))))

(defn set-supported-type
  [{:keys [db]} [supported-type]]
  {:db (assoc db :biometric/supported-type supported-type)})

(schema/=> set-supported-type events-schema/?set-supported-type)
(rf/reg-event-fx :biometric/set-supported-type set-supported-type)

(defn show-message
  [_ [code]]
  (let [content (if (#{:biometrics/not-enrolled-error
                       :biometrics/not-available-error}
                     code)
                  (i18n/label :t/grant-face-id-permissions)
                  (i18n/label :t/biometric-auth-error {:code code}))]
    {:fx [[:effects.utils/show-popup
           {:title   (i18n/label :t/biometric-auth-login-error-title)
            :content content}]]}))

(schema/=> show-message events-schema/?show-message)
(rf/reg-event-fx :biometric/show-message show-message)

(rf/reg-fx
 :biometric/authenticate
 (fn [{:keys [on-success on-fail on-cancel prompt-message on-done]}]
   (-> (biometrics/authenticate
        {:prompt-message          (or prompt-message (i18n/label :t/biometric-auth-reason-login))
         :fallback-prompt-message (i18n/label
                                   :t/biometric-auth-login-ios-fallback-label)
         :cancel-button-text      (i18n/label :t/cancel)})
       (.then (fn [not-canceled?]
                (utils/handle-cb on-done)
                (if not-canceled?
                  (utils/handle-cb on-success)
                  (utils/handle-cb on-cancel))))
       (.catch (fn [err]
                 (utils/handle-cb on-done)
                 (utils/handle-cb on-fail err))))))

(defn authenticate
  [{:keys [db]} [opts]]
  (let [pending? (get db :biometric/auth-pending?)]
    ;;NOTE: prompting biometric check while another one is pending triggers error
    (when-not pending?
      {:db (assoc db :biometric/auth-pending? true)
       :fx [[:biometric/authenticate
             (assoc opts :on-done [:set :biometric/auth-pending? false])]]})))

(schema/=> authenticate events-schema/?authenticate)
(rf/reg-event-fx :biometric/authenticate authenticate)

(defn enable-biometrics
  [{:keys [db]} [password]]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:db (assoc db :auth-method constants/auth-method-biometric)
     :fx [[:dispatch
           [:keychain/save-password-and-auth-method
            {:key-uid         key-uid
             :masked-password password}]]]}))

(schema/=> enable-biometrics events-schema/?enable-biometrics)
(rf/reg-event-fx :biometric/enable enable-biometrics)

(defn disable-biometrics
  [{:keys [db]}]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:db (assoc db :auth-method constants/auth-method-none)
     :fx [[:keychain/clear-user-password key-uid]]}))

(schema/=> disable-biometrics events-schema/?disable-biometrics)
(rf/reg-event-fx :biometric/disable disable-biometrics)

(defn check-if-biometrics-available
  [{:keys [key-uid on-success on-fail]}]
  (keychain/can-save-user-password?
   (fn [can-save?]
     (if-not can-save?
       (utils/handle-cb on-fail
                        (ex-info "cannot-save-user-password"
                                 {:fx :biometric/check-if-available}))
       (-> (biometrics/get-available)
           (.then (fn [available?]
                    (when-not available?
                      (throw (js/Error. "biometric-not-available")))))
           (.then #(keychain/get-auth-method! key-uid))
           (.then (fn [auth-method]
                    (when auth-method
                      (utils/handle-cb on-success auth-method))))
           (.catch (fn [err]
                     (let [message (.-message err)]
                       (utils/handle-cb on-fail
                                        (ex-info message
                                                 {:err err
                                                  :fx  :biometric/check-if-available}))
                       (when-not (= message "biometric-not-available")
                         (log/error "Failed to check if biometrics is available"
                                    {:error   err
                                     :key-uid key-uid
                                     :event   :profile.login/check-biometric}))))))))))

(schema/=> check-if-biometrics-available events-schema/?check-if-biometrics-available)
(rf/reg-fx :biometric/check-if-available check-if-biometrics-available)
