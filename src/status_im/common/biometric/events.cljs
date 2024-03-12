(ns status-im.common.biometric.events
  (:require
    [status-im.constants :as constants]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn set-supported-type
  [{:keys [db]} [supported-type]]
  {:db (assoc-in db [:biometrics :supported-type] supported-type)})

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

(rf/reg-event-fx :biometric/show-message show-message)

(defn on-authentication-done
  [{:keys [db]}]
  {:db (assoc-in db [:biometrics :auth-pending?] false)})

(rf/reg-event-fx :biometric/on-authentication-done on-authentication-done)

(defn authenticate
  [{:keys [db]} [opts]]
  (let [pending? (get-in db [:biometrics :auth-pending?])]
    ;;NOTE: prompting biometric check while another one is pending triggers error
    (when-not pending?
      {:db (assoc-in db [:biometrics :auth-pending?] true)
       :fx [[:effects.biometric/authenticate
             (assoc opts :on-done #(rf/dispatch [:biometric/on-authentication-done]))]]})))

(rf/reg-event-fx :biometric/authenticate authenticate)

(defn enable-biometrics
  [{:keys [db]} [password]]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:db (assoc db :auth-method constants/auth-method-biometric)
     :fx [[:dispatch
           [:keychain/save-password-and-auth-method
            {:key-uid         key-uid
             :masked-password password}]]]}))

(rf/reg-event-fx :biometric/enable enable-biometrics)

(defn disable-biometrics
  [{:keys [db]}]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:db (assoc db :auth-method constants/auth-method-none)
     :fx [[:keychain/clear-user-password key-uid]]}))

(rf/reg-event-fx :biometric/disable disable-biometrics)

