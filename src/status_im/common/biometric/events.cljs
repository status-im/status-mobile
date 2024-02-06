(ns status-im.common.biometric.events
  (:require
    [react-native.biometrics :as biometrics]
    [schema.core :as schema]
    [status-im.common.biometric.utils :as utils]
    [status-im.common.keychain.events :as keychain]
    [status-im.constants :as constants]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

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

(schema/=> set-supported-type
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple
      [:enum
       constants/biometrics-type-android
       constants/biometrics-type-face-id
       constants/biometrics-type-touch-id]]]]
   :schema.re-frame/event-fx])

(rf/reg-event-fx :biometric/set-supported-type set-supported-type)

(defn show-message
  [_ [code]]
  (let [content (if (#{::biometrics/not-enrolled
                       ::biometrics/not-available}
                     code)
                  (i18n/label :t/grant-face-id-permissions)
                  (i18n/label :t/biometric-auth-error {:code code}))]
    {:fx [[:effects.utils/show-popup
           {:title   (i18n/label :t/biometric-auth-login-error-title)
            :content content}]]}))

(schema/=> show-message
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple keyword?]]]
   :schema.re-frame/event-fx])

(rf/reg-event-fx :biometric/show-message show-message)

(rf/reg-fx
 :biometric/authenticate
 (fn [{:keys [on-success on-fail prompt-message]}]
   (-> (biometrics/authenticate
        {:prompt-message          (or prompt-message (i18n/label :t/biometric-auth-reason-login))
         :fallback-prompt-message (i18n/label
                                   :t/biometric-auth-login-ios-fallback-label)
         :cancel-button-text      (i18n/label :t/cancel)})
       (.then (fn [not-canceled?]
                (when (and on-success not-canceled?)
                  (on-success))))
       (.catch (fn [err]
                 (when on-fail
                   (on-fail err)))))))

(defn authenticate
  [_ [opts]]
  {:fx [[:biometric/authenticate opts]]})

(schema/=> authenticate
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple
      [:map {:closed true}
       [:on-success {:optional true} fn?]
       [:on-fail {:optional true} fn?]
       [:prompt-message {:optional true} :string]]]]]
   :schema.re-frame/event-fx])

(rf/reg-event-fx :biometric/authenticate authenticate)

(defn enable-biometrics
  [{:keys [db]} [password]]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    (println "inside: " password)
    {:db (assoc db :auth-method constants/auth-method-biometric)
     :fx [[:dispatch
           [:keychain/save-password-and-auth-method
            {:key-uid         key-uid
             :masked-password password}]]]}))

(schema/=> enable-biometrics
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple
      [:fn {:error/message "Should be an instance of security/MaskedData"}
       (fn [pw] (instance? security/MaskedData pw))]]]]
   :schema.re-frame/event-fx])

(rf/reg-event-fx :biometric/enable enable-biometrics)

(defn disable-biometrics
  [{:keys [db]}]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:db (assoc db :auth-method constants/auth-method-none)
     :fx [[:keychain/clear-user-password key-uid]]}))

(schema/=> disable-biometrics
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]]
   :schema.re-frame/event-fx])

(rf/reg-event-fx :biometric/disable disable-biometrics)

(rf/reg-fx
 :biometric/check-if-available
 (fn [[key-uid callback]]
   (keychain/can-save-user-password?
    (fn [can-save?]
      (when can-save?
        (-> (biometrics/get-available)
            (.then (fn [available?]
                     (when-not available?
                       (throw (js/Error. "biometric-not-available")))))
            (.then #(keychain/get-auth-method! key-uid))
            (.then (fn [auth-method]
                     (when auth-method (callback auth-method))))
            (.catch (fn [err]
                      (when-not (= (.-message err) "biometric-not-available")
                        (log/error "Failed to check if biometrics is available"
                                   {:error   err
                                    :key-uid key-uid
                                    :event   :profile.login/check-biometric}))))))))))
