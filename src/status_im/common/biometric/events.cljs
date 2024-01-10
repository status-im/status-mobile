(ns status-im.common.biometric.events
  (:require
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [react-native.async-storage :as async-storage]
    [react-native.biometrics :as biometrics]
    [react-native.platform :as platform]
    [status-im.common.keychain.events :as keychain]
    [status-im.constants :as constants]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def android-device-blacklisted?
  (and platform/android? (= (:brand (native-module/get-device-model-info)) "bannedbrand")))

(defn get-label-by-type
  [biometric-type]
  (condp = biometric-type
    constants/biometrics-type-android (i18n/label :t/biometric-fingerprint)
    constants/biometrics-type-face-id (i18n/label :t/biometric-faceid)
    (i18n/label :t/biometric-touchid)))

(defn get-icon-by-type
  [biometric-type]
  (condp = biometric-type
    constants/biometrics-type-face-id :i/face-id
    :i/touch-id))

(re-frame/reg-fx
 :biometric/get-supported-biometric-type
 (fn []
   ;;NOTE: if we can't save user password, we can't use biometric
   (keychain/can-save-user-password?
    (fn [can-save?]
      (when (and can-save? (not android-device-blacklisted?))
        (-> (biometrics/get-supported-type)
            (.then (fn [type]
                     (rf/dispatch [:biometric/get-supported-biometric-type-success type])))))))))

(rf/defn get-supported-biometric-auth-success
  {:events [:biometric/get-supported-biometric-type-success]}
  [{:keys [db]} supported-type]
  {:db (assoc db :biometric/supported-type supported-type)})

(rf/defn show-message
  {:events [:biometric/show-message]}
  [_ code]
  (let [content (if (#{constants/biometric-error-not-available
                       constants/biometric-error-not-enrolled}
                     code)
                  (i18n/label :t/grant-face-id-permissions)
                  (i18n/label :t/biometric-auth-error {:code code}))]
    {:effects.utils/show-popup
     {:title   (i18n/label :t/biometric-auth-login-error-title)
      :content content}}))

(defn- supress-biometry-error-key
  [key-uid]
  (keyword (str "biometric/supress-not-enrolled-error-" key-uid)))

;; NOTE: if the account had biometrics registered, but it's not enrolled at the moment,
;; we should show the error message only once and supress further "NOT_ENROLLED" errors
;; until biometry is enrolled again. Note that we can only know that when :biometric/authenticate
;; is dispatched and fails with "NOT_ENROLLED", since :biometric/get-supported-biometric-type
;; only tells us what kind of biometric is available on the device, but it doesn't know of its
;; enrollment status.
(re-frame/reg-fx
 :biometric/supress-not-enrolled-error
 (fn [[key-uid dispatch-event]]
   (let [storage-key (supress-biometry-error-key key-uid)]
     (-> (async-storage/get-item storage-key identity)
         (.then (fn [item]
                  (when (not item)
                    (rf/dispatch dispatch-event)
                    (async-storage/set-item! storage-key true))))
         (.catch (fn [err]
                   (log/error "Couldn't supress biometry NOT_ENROLLED error"
                              {:key-uid key-uid
                               :event   :biometric/supress-not-enrolled-error
                               :error   err})))))))

;; NOTE: when biometrics is re-enrolled, we erase the flag in async-storage to assure
;; the "NOT_ENROLLED" error message will be shown again if biometrics is un-enrolled
;; in the future.
(re-frame/reg-fx
 :biometric/reset-not-enrolled-error
 (fn [key-uid]
   (let [storage-key (supress-biometry-error-key key-uid)]
     (-> (async-storage/get-item storage-key identity)
         (.then (fn [supress?]
                  (when supress?
                    (async-storage/set-item! storage-key nil))))
         (.catch (fn [err]
                   (log/error "Couldn't reset supressing biometry NOT_ENROLLED error"
                              {:key-uid key-uid
                               :event   :biometric/reset-not-enrolled-error
                               :error   err})))))))

(re-frame/reg-fx
 :biometric/authenticate
 (fn [{:keys [on-success on-fail prompt-message]}]
   (-> (biometrics/authenticate
        {:prompt-message          (or prompt-message (i18n/label :t/biometric-auth-reason-login))
         :fallback-prompt-message (i18n/label
                                   :t/biometric-auth-login-ios-fallback-label)
         :cancel-button-text      (i18n/label :t/cancel)})
       ;; NOTE: resolves to `false` when cancelled by user
       (.then #(when % on-success))
       (.catch (fn [err]
                 (-> err
                     (.-message)
                     (on-fail)))))))

(rf/defn authenticate
  {:events [:biometric/authenticate]}
  [_ opts]
  {:biometric/authenticate opts})

(rf/reg-event-fx
 :biometric/on-enable-success
 (fn [{:keys [db]} [password]]
   (let [key-uid (get-in db [:profile/profile :key-uid])]
     {:db       (assoc db :auth-method constants/auth-method-biometric)
      :dispatch [:keychain/save-password-and-auth-method
                 {:key-uid         key-uid
                  :masked-password password}]})))

(rf/reg-event-fx
 :biometric/enable
 (fn [_ [password]]
   {:dispatch [:biometric/authenticate
               {:on-success #(rf/dispatch [:biometric/on-enable-success password])
                :on-fail    #(rf/dispatch [:biometric/show-message %])}]}))

(rf/reg-event-fx
 :biometric/disable
 (fn [{:keys [db]}]
   (let [key-uid (get-in db [:profile/profile :key-uid])]
     {:db                           (assoc db :auth-method constants/auth-method-none)
      :keychain/clear-user-password key-uid})))
