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
  (log/debug "[keycard] check-nfc-support")
  (when (and config/hardwallet-enabled?
             platform/android?)
    (.. keycard
        nfcIsSupported
        (then #(re-frame/dispatch [:hardwallet.callback/check-nfc-support-success %])))))

(defn check-nfc-enabled []
  (log/debug "[keycard] check-nfc-enabled")
  (when (and config/hardwallet-enabled?
             platform/android?)
    (.. keycard
        nfcIsEnabled
        (then #(re-frame/dispatch [:hardwallet.callback/check-nfc-enabled-success %])))))

(defn open-nfc-settings []
  (log/debug "[keycard] open-nfc-settings")
  (when platform/android?
    (.openNfcSettings keycard)))

(defn remove-event-listeners []
  (doseq [event ["keyCardOnConnected" "keyCardOnDisconnected"]]
    (.removeAllListeners event-emitter event)))

(defn register-card-events []
  (log/debug "[keycard] register-card-events")
  (when (and config/hardwallet-enabled?
             platform/android?)

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
  (log/debug "[keycard] get-application-info")
  (.. keycard
      (getApplicationInfo (str pairing))
      (then #(re-frame/dispatch [:hardwallet.callback/on-get-application-info-success % on-success]))
      (catch #(re-frame/dispatch [:hardwallet.callback/on-get-application-info-error (error-object->map %)]))))

(defn install-applet []
  (log/debug "[keycard] install-applet")
  (when config/hardwallet-enabled?
    (.. keycard
        installApplet
        (then #(re-frame/dispatch [:hardwallet.callback/on-install-applet-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-install-applet-error (error-object->map %)])))))

(defn init-card [pin]
  (log/debug "[keycard] init-card")
  (when config/hardwallet-enabled?
    (.. keycard
        (init pin)
        (then #(re-frame/dispatch [:hardwallet.callback/on-init-card-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-init-card-error (error-object->map %)])))))

(defn install-applet-and-init-card [pin]
  (log/debug "[keycard] install-applet-and-init-card")
  (when config/hardwallet-enabled?
    (.. keycard
        (installAppletAndInitCard pin)
        (then #(re-frame/dispatch [:hardwallet.callback/on-install-applet-and-init-card-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-install-applet-and-init-card-error (error-object->map %)])))))

(defn pair
  [{:keys [password]}]
  (log/debug "[keycard] pair")
  (when password
    (.. keycard
        (pair password)
        (then #(re-frame/dispatch [:hardwallet.callback/on-pair-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-pair-error (error-object->map %)])))))

(defn generate-mnemonic
  [{:keys [pairing words]}]
  (log/debug "[keycard] generate-mnemonic")
  (when pairing
    (.. keycard
        (generateMnemonic pairing words)
        (then #(re-frame/dispatch [:hardwallet.callback/on-generate-mnemonic-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-generate-mnemonic-error (error-object->map %)])))))

(defn generate-and-load-key
  [{:keys [mnemonic pairing pin]}]
  (log/debug "[keycard] generate-and-load-key")
  (when pairing
    (.. keycard
        (generateAndLoadKey mnemonic pairing pin)
        (then #(re-frame/dispatch [:hardwallet.callback/on-generate-and-load-key-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-generate-and-load-key-error (error-object->map %)])))))

(defn unblock-pin
  [{:keys [puk new-pin pairing]}]
  (log/debug "[keycard] unblock-pin")
  (when (and pairing new-pin puk)
    (.. keycard
        (unblockPin pairing puk new-pin)
        (then #(re-frame/dispatch [:hardwallet.callback/on-unblock-pin-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-unblock-pin-error (error-object->map %)])))))

(defn verify-pin
  [{:keys [pin pairing]}]
  (log/debug "[keycard] verify-pin")
  (when (and pairing (not-empty pin))
    (.. keycard
        (verifyPin pairing pin)
        (then #(re-frame/dispatch [:hardwallet.callback/on-verify-pin-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-verify-pin-error (error-object->map %)])))))

(defn change-pin
  [{:keys [current-pin new-pin pairing]}]
  (log/debug "[keycard] change-pin")
  (when (and pairing current-pin new-pin)
    (.. keycard
        (changePin pairing current-pin new-pin)
        (then #(re-frame/dispatch [:hardwallet.callback/on-change-pin-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-change-pin-error (error-object->map %)])))))

(defn unpair
  [{:keys [pin pairing]}]
  (log/debug "[keycard] unpair")
  (when (and pairing pin)
    (.. keycard
        (unpair pairing pin)
        (then #(re-frame/dispatch [:hardwallet.callback/on-unpair-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-unpair-error (error-object->map %)])))))

(defn delete
  []
  (log/debug "[keycard] delete")
  (.. keycard
      (delete)
      (then #(re-frame/dispatch [:hardwallet.callback/on-delete-success %]))
      (catch #(re-frame/dispatch [:hardwallet.callback/on-delete-error (error-object->map %)]))))

(defn remove-key
  [{:keys [pin pairing]}]
  (log/debug "[keycard] remove-key")
  (.. keycard
      (removeKey pairing pin)
      (then #(re-frame/dispatch [:hardwallet.callback/on-remove-key-success %]))
      (catch #(re-frame/dispatch [:hardwallet.callback/on-remove-key-error (error-object->map %)]))))

(defn remove-key-with-unpair
  [{:keys [pin pairing]}]
  (log/debug "[keycard] remove-key-with-unpair")
  (.. keycard
      (removeKeyWithUnpair pairing pin)
      (then #(re-frame/dispatch [:hardwallet.callback/on-remove-key-success %]))
      (catch #(re-frame/dispatch [:hardwallet.callback/on-remove-key-error (error-object->map %)]))))

(defn unpair-and-delete
  [{:keys [pin pairing]}]
  (log/debug "[keycard] unpair-and-delete")
  (when (and pairing pin)
    (.. keycard
        (unpairAndDelete pairing pin)
        (then #(re-frame/dispatch [:hardwallet.callback/on-delete-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-delete-error (error-object->map %)])))))

(defn get-keys
  [{:keys [pairing pin on-success]}]
  (log/debug "[keycard] get-keys")
  (when (and pairing (not-empty pin))
    (.. keycard
        (getKeys pairing pin)
        (then #(re-frame/dispatch [(or on-success :hardwallet.callback/on-get-keys-success) %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-get-keys-error (error-object->map %)])))))

(defn sign
  [{:keys [pairing pin hash]}]
  (log/debug "[keycard] sign")
  (when (and pairing pin hash)
    (.. keycard
        (sign pairing pin hash)
        (then #(re-frame/dispatch [:hardwallet.callback/on-sign-success %]))
        (catch #(re-frame/dispatch [:hardwallet.callback/on-sign-error (error-object->map %)])))))
