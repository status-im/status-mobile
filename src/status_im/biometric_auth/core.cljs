(ns status-im.biometric-auth.core
  (:require [re-frame.core :as re-frame]
            [status-im.utils.platform :as platform]
            [status-im.i18n :as i18n]
            [status-im.utils.fx :as fx]
            [status-im.ui.components.colors :as colors]
            [status-im.native-module.core :as status]
            [status-im.react-native.js-dependencies :as rn]))

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
               :fallbackLabel (or ios-fallback-label "")})
            (when platform/android?
              {:title (i18n/label :t/biometric-auth-android-title)
               :imageColor colors/blue
               :imageErrorColor colors/red
               :sensorDescription (i18n/label :t/biometric-auth-android-sensor-desc)
               :sensorErrorDescription (i18n/label :t/biometric-auth-android-sensor-error-desc)
               :cancelText (i18n/label :cancel)}))))

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
     :bioauth-code code
     :bioauth-message (get-error-message code)}))

(defn- do-get-supported [callback]
  (-> (.isSupported (rn/touchid))
      (.then #(callback (or (keyword %) android-default-support)))
      (.catch #(callback nil))))

(defn get-supported [callback]
  (cond platform/ios? (do-get-supported callback)
        platform/android? (if android-device-blacklisted?
                            (callback false)
                            (do-get-supported callback))
        :else (callback false)))

(defn authenticate
  ([cb]
   (authenticate cb nil))
  ([cb {:keys [reason ios-fallback-label]}]
   (-> (.authenticate (rn/touchid) reason (authenticate-options ios-fallback-label))
       (.then #(cb success-result))
       (.catch #(cb (generate-error-result %))))))

(fx/defn authenticate-fx
  [_ cb options]
  {:biometric-auth/authenticate [cb options]})

(re-frame/reg-fx
 :biometric-auth/authenticate
 (fn [[cb options]]
   (authenticate #(cb %) options)))