(ns status-im.multiaccounts.biometric.core
  (:require
   [status-im.utils.fx :as fx]
   [status-im.utils.types :as types]
   [clojure.string :as string]
   [status-im.popover.core :as popover]
   [status-im.native-module.core :as status]
   [status-im.utils.platform :as platform]
   [status-im.ui.components.colors :as colors]
   [status-im.i18n :as i18n]
   [status-im.react-native.js-dependencies :as js-dependencies]
   [re-frame.core :as re-frame]
   [status-im.ethereum.json-rpc :as json-rpc]
   [status-im.utils.keychain.core :as keychain]
   [taoensso.timbre :as log]))

;; currently, for android, react-native-touch-id
;; is not returning supported biometric type
;; defaulting to :fingerprint
(def android-default-support :fingerprint)

;;; android blacklist based on device info:

(def deviceinfo (status/get-device-model-info))

;; {:model     ?
;;   :brand     "Xiaomi"
;;   :build-id  "13D15"
;;   :device-id "goldfish"
;; more info on https://github.com/react-native-community/react-native-device-info

(def android-device-blacklisted?
  (cond
    (= (:brand deviceinfo) "bannedbrand") true
    :else false))

;; biometric auth config
;; https://github.com/naoufal/react-native-touch-id#authenticatereason-config
(defn- authenticate-options [ios-fallback-label]
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

(defn get-label [supported-biometric-auth]
  (case supported-biometric-auth
    :fingerprint "Fingerprint"
    :FaceID "Face ID"
    "Touch ID"))

(defn- get-error-message
  "must return an error message for the user"
  [touchid-error-code]
  (cond
    ;; no message if user canceled or falled back to password
    (= touchid-error-code "USER_CANCELED") nil
    (= touchid-error-code "USER_FALLBACK") nil
    ;; add here more specific errors if needed
    ;; https://github.com/naoufal/react-native-touch-id#unified-errors
    :else (i18n/label :t/biometric-auth-error {:code touchid-error-code})))

(def success-result
  {:bioauth-success true})

(defn- generate-error-result [touchid-error-obj]
  (let [code (aget touchid-error-obj "code")]
    {:bioauth-success false
     :bioauth-code    code
     :bioauth-message (get-error-message code)}))

(defn- do-get-supported [callback]
  (-> (.isSupported js-dependencies/touchid)
      (.then #(callback (or (keyword %) android-default-support)))
      (.catch #(callback nil))))

(defn get-supported [callback]
  (log/debug "[biometric] get-supported")
  (cond platform/ios? (do-get-supported callback)
        platform/android? (if android-device-blacklisted?
                            (callback nil)
                            (do-get-supported callback))
        :else (callback nil)))

(defn authenticate-fx
  ([cb]
   (authenticate-fx cb nil))
  ([cb {:keys [reason ios-fallback-label]}]
   (log/debug "[biometric] authenticate-fx")
   (-> (.authenticate js-dependencies/touchid reason (authenticate-options ios-fallback-label))
       (.then #(cb success-result))
       (.catch #(cb (generate-error-result %))))))

(re-frame/reg-fx
 :get-supported-biometric-auth
 (fn []
   (get-supported #(re-frame/dispatch [:init.callback/get-supported-biometric-auth-success %]))))

(fx/defn set-supported-biometric-auth
  {:events [:init.callback/get-supported-biometric-auth-success]}
  [{:keys [db]} supported-biometric-auth]
  {:db (assoc db :supported-biometric-auth supported-biometric-auth)})

(fx/defn authenticate
  [_ cb options]
  {:biometric-auth/authenticate [cb options]})

(re-frame/reg-fx
 :biometric-auth/authenticate
 (fn [[cb options]]
   (authenticate-fx #(cb %) options)))

(fx/defn update-biometric [{db :db :as cofx} biometric-auth?]
  (let [address (or (get-in db [:multiaccount :address])
                    (get-in db [:multiaccounts/login :address]))]
    (fx/merge cofx
              (keychain/save-auth-method
               address
               (if biometric-auth?
                 keychain/auth-method-biometric
                 keychain/auth-method-none))
              #(when-not biometric-auth?
                 {:keychain/clear-user-password address}))))

(fx/defn biometric-auth-switched
  {:events [:multiaccounts.ui/biometric-auth-switched]}
  [cofx biometric-auth?]
  (if biometric-auth?
    (authenticate
     cofx
     #(re-frame/dispatch [:biometric-init-done %])
     {})
    (update-biometric cofx false)))

(fx/defn show-message
  [cofx bioauth-message bioauth-code]
  (let [content (or (when (get #{"NOT_AVAILABLE" "NOT_ENROLLED"} bioauth-code)
                      (i18n/label :t/grant-face-id-permissions))
                    bioauth-message)]
    (when content
      {:utils/show-popup
       {:title (i18n/label :t/biometric-auth-login-error-title)
        :content content}})))

(fx/defn biometric-init-done
  {:events [:biometric-init-done]}
  [cofx {:keys [bioauth-success bioauth-message bioauth-code]}]
  (if bioauth-success
    (if (= keychain/auth-method-password
           (get-in cofx [:db :auth-method]))
      (update-biometric cofx true)
      (popover/show-popover cofx {:view :enable-biometric}))
    (show-message cofx bioauth-message bioauth-code)))

(fx/defn biometric-auth
  {:events [:biometric-authenticate]}
  [cofx]
  (authenticate
   cofx
   #(re-frame/dispatch [:biometric-auth-done %])
   {:reason             (i18n/label :t/biometric-auth-reason-login)
    :ios-fallback-label (i18n/label :t/biometric-auth-login-ios-fallback-label)}))

(fx/defn enable
  {:events [:biometric/enable]}
  [cofx]
  (fx/merge
   cofx
   (popover/hide-popover)
   (authenticate #(re-frame/dispatch [:biometric/setup-done %]) {})))

(fx/defn disable
  {:events [:biometric/disable]}
  [{:keys [db] :as cofx}]
  (fx/merge
   cofx
   {:db (-> db
            (assoc :auth-method keychain/auth-method-none)
            (assoc-in [:multiaccounts/login :save-password?] false))}
   (popover/hide-popover)))

(fx/defn setup-done
  {:events [:biometric/setup-done]}
  [{:keys [db] :as cofx} {:keys [bioauth-success bioauth-message bioauth-code]}]
  (log/debug "[biometric] setup-done"
             "bioauth-success" bioauth-success
             "bioauth-message" bioauth-message
             "bioauth-code" bioauth-code)
  (if bioauth-success
    {:db (assoc db :auth-method keychain/auth-method-biometric-prepare)}
    (show-message cofx bioauth-message bioauth-code)))
