(ns status-im.hardwallet.card
  (:require [re-frame.core :as re-frame]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [status-im.utils.config :as config]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]))

(defonce keycard (.-default js-dependencies/status-keycard))
(defonce event-emitter (.-DeviceEventEmitter js-dependencies/react-native))

(defn- error-object->map [object]
  {:code  (.-code object)
   :error (.-message object)})

(defn check-nfc-support []
  (when config/hardwallet-enabled?
    (.. keycard
        nfcIsSupported
        (then #(re-frame/dispatch [:hardwallet.callback/check-nfc-support-success %])))))

(defn check-nfc-enabled []
  (when (and platform/android?
             config/hardwallet-enabled?)
    (.. keycard
        nfcIsEnabled
        (then #(re-frame/dispatch [:hardwallet.callback/check-nfc-enabled-success %])))))

(defn open-nfc-settings []
  (when platform/android?
    (.openNfcSettings keycard)))

(defn remove-event-listeners []
  (doseq [event ["keyCardOnConnected" "keyCardOnDisconnected"]]
    (.removeAllListeners event-emitter event)))

(defn register-card-events []
  (when (and config/hardwallet-enabled?)

    (remove-event-listeners)

    (re-frame/dispatch [:hardwallet.callback/on-register-card-events
                        {:on-card-connected
                         (.addListener event-emitter
                                       "keyCardOnConnected"
                                       #(re-frame/dispatch [:hardwallet.callback/on-card-connected %]))

                         :on-card-disconnected
                         (.addListener event-emitter
                                       "keyCardOnDisconnected"
                                       #(re-frame/dispatch [:hardwallet.callback/on-card-disconnected %]))}])))

(defn get-application-info []
  (.. keycard
      getApplicationInfo
      (then #(re-frame/dispatch [:hardwallet.callback/on-get-application-info-success %]))
      (catch #(re-frame/dispatch [:hardwallet.callback/on-get-application-info-error (error-object->map %)]))))

(defn start []
  (when config/hardwallet-enabled?
    (.. keycard
        start
        (then #(log/debug "[hardwallet] module started"))
        (catch #(log/debug "[hardwallet] module not started " %)))))

(defn install-applet-and-init-card []
  (when config/hardwallet-enabled?
    (.. keycard
        installAppletAndInitCard
        (then #(re-frame/dispatch [:hardwallet.callback/on-install-applet-and-init-card-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-install-applet-and-init-card-error (error-object->map %)])))))

(defn pair
  [{:keys [password]}]
  (when password
    (.. keycard
        (pair password)
        (then #(re-frame/dispatch [:hardwallet.callback/on-pairing-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-pairing-error (error-object->map %)])))))
(defn generate-mnemonic
  [{:keys [pairing]}]
  (when pairing
    (.. keycard
        (generateMnemonic pairing)
        (then #(re-frame/dispatch [:hardwallet.callback/on-generate-mnemonic-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-generate-mnemonic-error (error-object->map %)])))))

(defn generate-and-load-key
  [{:keys [mnemonic pairing pin]}]
  (when pairing
    (.. keycard
        (generateAndLoadKey mnemonic pairing pin)
        (then #(re-frame/dispatch [:hardwallet.callback/on-generate-and-load-key-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-generate-and-load-key-error (error-object->map %)])))))
