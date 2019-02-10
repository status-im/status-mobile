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
  (when (and config/hardwallet-enabled?
             platform/android?)
    (.. keycard
        nfcIsSupported
        (then #(re-frame/dispatch [:hardwallet.callback/check-nfc-support-success %])))))

(defn check-nfc-enabled []
  (when (and config/hardwallet-enabled?
             platform/android?)
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

(defn get-application-info [{:keys [pairing on-success]}]
  (.. keycard
      (getApplicationInfo (str pairing))
      (then #(re-frame/dispatch [:hardwallet.callback/on-get-application-info-success % on-success]))
      (catch #(re-frame/dispatch [:hardwallet.callback/on-get-application-info-error (error-object->map %)]))))

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

(defn unblock-pin
  [{:keys [puk new-pin pairing]}]
  (when (and pairing new-pin puk)
    (.. keycard
        (unblockPin pairing puk new-pin)
        (then #(re-frame/dispatch [:hardwallet.callback/on-unblock-pin-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-unblock-pin-error (error-object->map %)])))))

(defn verify-pin
  [{:keys [pin pairing]}]
  (when (and pairing pin)
    (.. keycard
        (verifyPin pairing pin)
        (then #(re-frame/dispatch [:hardwallet.callback/on-verify-pin-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-verify-pin-error (error-object->map %)])))))

(defn change-pin
  [{:keys [current-pin new-pin pairing]}]
  (when (and pairing current-pin new-pin)
    (.. keycard
        (changePin pairing current-pin new-pin)
        (then #(re-frame/dispatch [:hardwallet.callback/on-change-pin-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-change-pin-error (error-object->map %)])))))

(defn unpair
  [{:keys [pin pairing]}]
  (when (and pairing pin)
    (.. keycard
        (unpair pairing pin)
        (then #(re-frame/dispatch [:hardwallet.callback/on-unpair-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-unpair-error (error-object->map %)])))))

(defn delete
  []
  (.. keycard
      (delete)
      (then #(re-frame/dispatch [:hardwallet.callback/on-delete-success %]))
      (catch #(re-frame/dispatch [:hardwallet.callback/on-delete-error (error-object->map %)]))))

(defn unpair-and-delete
  [{:keys [pin pairing]}]
  (when (and pairing pin)
    (.. keycard
        (unpairAndDelete pairing pin)
        (then #(re-frame/dispatch [:hardwallet.callback/on-delete-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-delete-error (error-object->map %)])))))

(defn get-keys
  [{:keys [pairing pin]}]
  (.. keycard
      (getKeys pairing pin)
      (then #(re-frame/dispatch [:hardwallet.callback/on-get-keys-success %]))
      (catch #(re-frame/dispatch [:hardwallet.callback/on-get-keys-error (error-object->map %)]))))
