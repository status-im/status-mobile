(ns status-im.contexts.keycard.effects
  (:require [keycard.keycard :as keycard]
            [native-module.core :as native-module]
            [react-native.async-storage :as async-storage]
            [react-native.platform :as platform]
            [status-im.contexts.profile.config :as profile.config]
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]
            [utils.transforms :as transforms]))

(defonce ^:private active-listeners (atom []))

(defn register-card-events
  []
  (doseq [listener @active-listeners]
    (keycard/remove-event-listener listener))
  (reset! active-listeners
    [(keycard/on-card-connected #(rf/dispatch [:keycard/on-card-connected]))
     (keycard/on-card-disconnected #(rf/dispatch [:keycard/on-card-disconnected]))
     (when platform/ios?
       (keycard/on-nfc-user-cancelled #(rf/dispatch [:keycard.ios/on-nfc-user-cancelled])))
     (when platform/ios?
       (keycard/on-nfc-timeout #(rf/dispatch [:keycard.ios/on-nfc-timeout])))
     (keycard/on-nfc-enabled #(rf/dispatch [:keycard/on-check-nfc-enabled-success true]))
     (keycard/on-nfc-disabled #(rf/dispatch [:keycard/on-check-nfc-enabled-success false]))]))
(rf/reg-fx :effects.keycard/register-card-events register-card-events)

(defn check-nfc-enabled
  []
  (log/debug "[keycard] check-nfc-enabled")
  (keycard/check-nfc-enabled
   {:on-success
    (fn [response]
      (log/debug "[keycard response] check-nfc-enabled")
      (rf/dispatch [:keycard/on-check-nfc-enabled-success response]))}))
(rf/reg-fx :effects.keycard/check-nfc-enabled check-nfc-enabled)

(rf/reg-fx
 :effects.keycard.ios/start-nfc
 (fn [args]
   (log/debug "fx start-nfc")
   (keycard/start-nfc args)))

(rf/reg-fx
 :effects.keycard.ios/stop-nfc
 (fn [args]
   (log/debug "fx stop-nfc")
   (keycard/stop-nfc args)))

(defn- error-object->map
  [^js object]
  {:code  (.-code object)
   :error (.-message object)})

(defn get-application-info
  [{:keys [on-success on-failure] :as args}]
  (log/debug "[keycard] get-application-info")
  (keycard/get-application-info
   (assoc
    args
    :on-success
    (fn [response]
      (log/debug "[keycard response succ] get-application-info")
      (when on-success
        (on-success response)))
    :on-failure
    (fn [response]
      (log/error "[keycard response fail] get-application-info")
      (when on-failure
        (on-failure (error-object->map response)))))))
(rf/reg-fx :effects.keycard/get-application-info get-application-info)

(defn get-keys
  [{:keys [on-success on-failure] :as args}]
  (log/debug "[keycard] get-keys")
  (keycard/get-keys
   (assoc
    args
    :on-success
    (fn [response]
      (log/debug "[keycard response succ] get-keys")
      (when on-success
        (on-success (transforms/js->clj response))))
    :on-failure
    (fn [response]
      (log/warn "[keycard response fail] get-keys"
                (error-object->map response))
      (when on-failure
        (on-failure (error-object->map response)))))))
(rf/reg-fx :effects.keycard/get-keys get-keys)

(defn login
  [{:keys [key-uid password whisper-private-key]}]
  (native-module/login-account
   (assoc (profile.config/login)
          :keyUid                   key-uid
          :password                 password
          :keycardWhisperPrivateKey whisper-private-key)))
(rf/reg-fx :effects.keycard/login-with-keycard login)

(defn retrieve-pairings
  []
  (async-storage/get-item
   "status-keycard-pairings"
   #(rf/dispatch [:keycard/on-retrieve-pairings-success %])))
(rf/reg-fx :effects.keycard/retrieve-pairings retrieve-pairings)

(defn set-pairing-to-keycard
  [pairings]
  (keycard/set-pairings pairings))
(rf/reg-fx :effects.keycard/set-pairing-to-keycard set-pairing-to-keycard)
