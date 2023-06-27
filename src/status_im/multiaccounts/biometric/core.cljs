(ns status-im.multiaccounts.biometric.core
  (:require ["react-native-touch-id" :default touchid]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [utils.i18n :as i18n]
            [native-module.core :as native-module]
            [status-im.popover.core :as popover]
            [utils.re-frame :as rf]
            [status-im.utils.keychain.core :as keychain]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]))

;; currently, for android, react-native-touch-id
;; is not returning supported biometric type
;; defaulting to :fingerprint
(def android-default-support :fingerprint)

;;; android blacklist based on device info:

(def deviceinfo (native-module/get-device-model-info))

;; {:model     ?
;;   :brand     "Xiaomi"
;;   :build-id  "13D15"
;;   :device-id "goldfish"
;; more info on https://github.com/react-native-community/react-native-device-info

(def android-device-blacklisted?
  (cond
    (= (:brand deviceinfo) "bannedbrand") true
    :else                                 false))

;; biometric auth config
;; https://github.com/naoufal/react-native-touch-id#authenticatereason-config
(defn- authenticate-options
  [ios-fallback-label]
  (clj->js (merge
            {:unifiedErrors true}
            (when platform/ios?
              {:passcodeFallback false
               :fallbackLabel    (or ios-fallback-label "")})
            (when platform/android?
              {:title                  (i18n/label :t/biometric-auth-android-title)
               :imageColor             colors/blue
               :imageErrorColor        colors/red
               :sensorDescription      (i18n/label :t/biometric-auth-android-sensor-desc)
               :sensorErrorDescription (i18n/label :t/biometric-auth-android-sensor-error-desc)
               :cancelText             (i18n/label :cancel)}))))

(defn get-label
  [supported-biometric-auth]
  (case supported-biometric-auth
    :fingerprint (i18n/label :t/biometric-fingerprint)
    :FaceID      (i18n/label :t/biometric-faceid)
    (i18n/label :t/biometric-touchid)))

(defn- get-error-message
  "must return an error message for the user"
  [touchid-error-code]
  (cond
    ;; no message if user canceled or falled back to password
    (= touchid-error-code "USER_CANCELED") nil
    (= touchid-error-code "USER_FALLBACK") nil
    ;; add here more specific errors if needed
    ;; https://github.com/naoufal/react-native-touch-id#unified-errors
    :else                                  (i18n/label :t/biometric-auth-error
                                                       {:code touchid-error-code})))

(def success-result
  {:bioauth-success true})

(defn- generate-error-result
  [touchid-error-obj]
  (let [code (aget touchid-error-obj "code")]
    {:bioauth-success false
     :bioauth-code    code
     :bioauth-message (get-error-message code)}))

(defn- do-get-supported
  [callback]
  (-> (.isSupported touchid)
      (.then #(callback (or (keyword %) android-default-support)))
      (.catch #(callback nil))))

(defn get-supported
  [callback]
  (log/debug "[biometric] get-supported")
  (cond platform/ios?     (do-get-supported callback)
        platform/android? (if android-device-blacklisted?
                            (callback nil)
                            (do-get-supported callback))
        :else             (callback nil)))

(defn authenticate-fx
  ([cb]
   (authenticate-fx cb nil))
  ([cb {:keys [reason ios-fallback-label]}]
   (log/debug "[biometric] authenticate-fx")
   (-> (.authenticate touchid reason (authenticate-options ios-fallback-label))
       (.then #(cb success-result))
       (.catch #(cb (generate-error-result %))))))

(re-frame/reg-fx
 :get-supported-biometric-auth
 (fn []
   (let [callback #(re-frame/dispatch [:init.callback/get-supported-biometric-auth-success %])]
     ;;NOTE: if we can't save user password, we can't support biometrics
     (keychain/can-save-user-password?
      (fn [can-save?]
        (if can-save?
          (get-supported callback)
          (callback nil)))))))

(rf/defn set-supported-biometric-auth
  {:events [:init.callback/get-supported-biometric-auth-success]}
  [{:keys [db]} supported-biometric-auth]
  {:db (assoc db :supported-biometric-auth supported-biometric-auth)})

(rf/defn authenticate
  [_ cb options]
  {:biometric-auth/authenticate [cb options]})

(re-frame/reg-fx
 :biometric-auth/authenticate
 (fn [[cb options]]
   (authenticate-fx #(cb %) options)))

(re-frame/reg-fx
 :biometric/enable-and-save-password
 (fn [{:keys [key-uid
              masked-password
              on-success
              on-error]}]
   (-> (keychain/save-user-password!
        key-uid
        masked-password)
       (.then
        (fn [_]
          (keychain/save-auth-method!
           key-uid
           keychain/auth-method-biometric)))
       (.then
        (fn [_]
          (when on-success
            (on-success))))
       (.catch (fn [error]
                 (when on-error
                   (on-error error)))))))

(rf/defn update-biometric
  [{db :db :as cofx} biometric-auth?]
  (let [key-uid (or (get-in db [:profile/profile :key-uid])
                    (get-in db [:profile/login :key-uid]))]
    (rf/merge cofx
              (keychain/save-auth-method
               key-uid
               (if biometric-auth?
                 keychain/auth-method-biometric
                 keychain/auth-method-none))
              #(when-not biometric-auth?
                 {:keychain/clear-user-password key-uid}))))

(rf/defn biometric-auth-switched
  {:events [:multiaccounts.ui/biometric-auth-switched]}
  [cofx biometric-auth?]
  (if biometric-auth?
    (authenticate
     cofx
     #(re-frame/dispatch [:biometric-init-done %])
     {})
    (update-biometric cofx false)))

(rf/defn show-message
  [cofx bioauth-message bioauth-code]
  (let [content (or (when (get #{"NOT_AVAILABLE" "NOT_ENROLLED"} bioauth-code)
                      (i18n/label :t/grant-face-id-permissions))
                    bioauth-message)]
    (when content
      {:utils/show-popup
       {:title   (i18n/label :t/biometric-auth-login-error-title)
        :content content}})))

(rf/defn biometric-init-done
  {:events [:biometric-init-done]}
  [cofx {:keys [bioauth-success bioauth-message bioauth-code]}]
  (if bioauth-success
    (if (= keychain/auth-method-password
           (get-in cofx [:db :auth-method]))
      (update-biometric cofx true)
      (popover/show-popover cofx {:view :enable-biometric}))
    (show-message cofx bioauth-message bioauth-code)))

(rf/defn biometric-auth
  {:events [:biometric-authenticate]}
  [cofx]
  (authenticate
   cofx
   #(re-frame/dispatch [:biometric-auth-done %])
   {:reason             (i18n/label :t/biometric-auth-reason-login)
    :ios-fallback-label (i18n/label :t/biometric-auth-login-ios-fallback-label)}))

(rf/defn enable
  {:events [:biometric/enable]}
  [cofx]
  (rf/merge
   cofx
   (popover/hide-popover)
   (authenticate #(re-frame/dispatch [:biometric/setup-done %]) {})))

(rf/defn disable
  {:events [:biometric/disable]}
  [{:keys [db] :as cofx}]
  (rf/merge
   cofx
   {:db (-> db
            (assoc :auth-method keychain/auth-method-none)
            (assoc-in [:profile/login :save-password?] false))}
   (popover/hide-popover)))

(rf/defn setup-done
  {:events [:biometric/setup-done]}
  [{:keys [db] :as cofx} {:keys [bioauth-success bioauth-message bioauth-code]}]
  (log/debug "[biometric] setup-done"
             "bioauth-success" bioauth-success
             "bioauth-message" bioauth-message
             "bioauth-code"    bioauth-code)
  (if bioauth-success
    {:db (assoc db :auth-method keychain/auth-method-biometric-prepare)}
    (show-message cofx bioauth-message bioauth-code)))
