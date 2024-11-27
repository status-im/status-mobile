(ns status-im.contexts.keycard.effects
  (:require [keycard.keycard :as keycard]
            [native-module.core :as native-module]
            [promesa.core :as promesa]
            [react-native.async-storage :as async-storage]
            [react-native.platform :as platform]
            status-im.contexts.keycard.nfc.effects
            [status-im.contexts.keycard.utils :as keycard.utils]
            [status-im.contexts.profile.config :as profile.config]
            [utils.hex :as hex]
            [utils.re-frame :as rf]))

(defonce ^:private active-listeners (atom []))

(defn unregister-card-events
  []
  (doseq [listener @active-listeners]
    (keycard/remove-event-listener listener))
  (reset! active-listeners nil))

(defn register-card-events
  []
  (unregister-card-events)
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
(rf/reg-fx :effects.keycard/unregister-card-events unregister-card-events)

(rf/reg-fx :effects.keycard/get-application-info
 (fn [args]
   (keycard/get-application-info (keycard.utils/wrap-handlers args))))

(rf/reg-fx :effects.keycard/get-keys
 (fn [args]
   (keycard/get-keys (keycard.utils/wrap-handlers args))))

(rf/reg-fx :effects.keycard/get-more-keys
 (fn [args]
   (keycard/import-keys (keycard.utils/wrap-handlers args))))

(rf/reg-fx :effects.keycard/export-key
 (fn [args]
   (keycard/export-key (keycard.utils/wrap-handlers args))))

(rf/reg-fx :effects.keycard/sign
 (fn [args]
   (-> (keycard/sign args)
       (promesa/then (keycard.utils/get-on-success args))
       (promesa/catch (keycard.utils/get-on-failure args)))))

(rf/reg-fx :effects.keycard/sign-hashes
 (fn [{:keys [hashes pin path on-success] :as args}]
   (-> (promesa/all
        (for [hash-data hashes]
          (-> (keycard/sign {:pin       pin
                             :path      path
                             :hash-data (hex/normalize-hex hash-data)})
              (promesa/then (fn [signature]
                              {:signature signature
                               :message   hash-data})))))
       (promesa/then on-success)
       (promesa/catch (keycard.utils/get-on-failure args)))))

(rf/reg-fx :keycard/init-card
 (fn [args]
   (keycard/init-card (keycard.utils/wrap-handlers args))))

(rf/reg-fx :effects.keycard/generate-and-load-key
 (fn [args]
   (keycard/generate-and-load-key (keycard.utils/wrap-handlers args))))

(rf/reg-fx :effects.keycard/login-with-keycard
 (fn [{:keys [key-uid password whisper-private-key]}]
   (native-module/login-account
    (assoc (profile.config/login)
           :keyUid                   key-uid
           :password                 password
           :keycardWhisperPrivateKey whisper-private-key))))

(rf/reg-fx :effects.keycard/set-pairing-to-keycard
 (fn [pairings]
   (keycard/set-pairings pairings)))

(rf/reg-fx
 :keycard/persist-pairings
 (fn [pairings]
   (async-storage/set-item! "status-keycard-pairings" pairings)))

(rf/reg-fx :effects.keycard/retrieve-pairings
 (fn []
   (async-storage/get-item
    "status-keycard-pairings"
    #(rf/dispatch [:keycard/on-retrieve-pairings-success %]))))
