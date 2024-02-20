(ns status-im.common.biometric.events
  (:require
    [schema.core :as schema]
    [status-im.common.biometric.events-schema :as events-schema]
    [status-im.constants :as constants]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn set-supported-type
  [{:keys [db]} [supported-type]]
  {:db (assoc-in db [:biometrics :supported-type] supported-type)})

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

(defn authenticate
  [{:keys [db]} [opts]]
  (let [pending? (get-in db [:biometrics :auth-pending?])]
    ;;NOTE: prompting biometric check while another one is pending triggers error
    (when-not pending?
      {:db (assoc-in db [:biometrics :auth-pending?] true)
       :fx [[:effects.biometric/authenticate
             (assoc opts :on-done #(rf/dispatch [:set-in [:biometrics :auth-pending?] false]))]]})))

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

