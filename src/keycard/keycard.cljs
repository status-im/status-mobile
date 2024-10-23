(ns keycard.keycard
  (:require
    ["react-native" :as rn]
    ["react-native-status-keycard" :default status-keycard]
    [react-native.platform :as platform]
    [taoensso.timbre :as log]))

(defonce event-emitter
  (if platform/ios?
    (new (.-NativeEventEmitter rn) status-keycard)
    (.-DeviceEventEmitter rn)))

(defn start-nfc
  [{:keys [on-success on-failure prompt-message]}]
  (log/debug "start-nfc")
  (.. status-keycard
      (startNFC (str prompt-message))
      (then on-success)
      (catch on-failure)))

(defn stop-nfc
  [{:keys [on-success on-failure error-message]}]
  (log/debug "stop-nfc")
  (.. status-keycard
      (stopNFC (str error-message))
      (then on-success)
      (catch on-failure)))

(defn set-nfc-message
  [{:keys [on-success on-failure status-message]}]
  (log/debug "set-nfc-message")
  (.. status-keycard
      (setNFCMessage (str status-message))
      (then on-success)
      (catch on-failure)))

(defn check-nfc-support
  [{:keys [on-success]}]
  (.. status-keycard
      nfcIsSupported
      (then on-success)))

(defn check-nfc-enabled
  [{:keys [on-success]}]
  (.. status-keycard
      nfcIsEnabled
      (then on-success)))

(defn open-nfc-settings
  []
  (.openNfcSettings status-keycard))

(defn remove-event-listeners
  []
  (doseq [event ["keyCardOnConnected" "keyCardOnDisconnected" "keyCardOnNFCUserCancelled"
                 "keyCardOnNFCTimeout"]]
    (.removeAllListeners ^js event-emitter event)))

(defn remove-event-listener
  [^js event]
  (when event
    (.remove event)))

(defn on-card-connected
  [callback]
  (.addListener ^js event-emitter "keyCardOnConnected" callback))

(defn on-card-disconnected
  [callback]
  (.addListener ^js event-emitter "keyCardOnDisconnected" callback))

(defn on-nfc-user-cancelled
  [callback]
  (.addListener ^js event-emitter "keyCardOnNFCUserCancelled" callback))

(defn on-nfc-timeout
  [callback]
  (.addListener ^js event-emitter "keyCardOnNFCTimeout" callback))

(defn on-nfc-enabled
  [callback]
  (.addListener ^js event-emitter "keyCardOnNFCEnabled" callback))

(defn on-nfc-disabled
  [callback]
  (.addListener ^js event-emitter "keyCardOnNFCDisabled" callback))

(defn set-pairings
  [pairings]
  (.. status-keycard (setPairings (clj->js (or pairings {})))))

(defn get-application-info
  [{:keys [on-success on-failure]}]
  (.. status-keycard
      (getApplicationInfo)
      (then on-success)
      (catch on-failure)))

(defn factory-reset
  [{:keys [on-success on-failure]}]
  (.. status-keycard
      (factoryReset)
      (then on-success)
      (catch on-failure)))

(defn install-applet
  [{:keys [on-success on-failure]}]
  (.. status-keycard
      installApplet
      (then on-success)
      (catch on-failure)))

(defn install-cash-applet
  [{:keys [on-success on-failure]}]
  (.. status-keycard
      installCashApplet
      (then on-success)
      (catch on-failure)))

(defn init-card
  [{:keys [pin on-success on-failure]}]
  (.. status-keycard
      (init pin)
      (then on-success)
      (catch on-failure)))

(defn install-applet-and-init-card
  [{:keys [pin on-success on-failure]}]
  (.. status-keycard
      (installAppletAndInitCard pin)
      (then on-success)
      (catch on-failure)))

(defn pair
  [{:keys [password on-success on-failure]}]
  (when password
    (.. status-keycard
        (pair password)
        (then on-success)
        (catch on-failure))))

(defn generate-and-load-key
  [{:keys [mnemonic pin on-success on-failure]}]
  (.. status-keycard
      (generateAndLoadKey mnemonic pin)
      (then on-success)
      (catch on-failure)))

(defn save-mnemonic
  [{:keys [mnemonic pin on-success on-failure]}]
  (.. status-keycard
      (saveMnemonic mnemonic pin)
      (then on-success)
      (catch on-failure)))

(defn unblock-pin
  [{:keys [puk new-pin on-success on-failure]}]
  (when (and new-pin puk)
    (.. status-keycard
        (unblockPin puk new-pin)
        (then on-success)
        (catch on-failure))))

(defn verify-pin
  [{:keys [pin on-success on-failure]}]
  (when (not-empty pin)
    (.. status-keycard
        (verifyPin pin)
        (then on-success)
        (catch on-failure))))

(defn change-pin
  [{:keys [current-pin new-pin on-success on-failure]}]
  (when (and current-pin new-pin)
    (.. status-keycard
        (changePin current-pin new-pin)
        (then on-success)
        (catch on-failure))))

(defn change-puk
  [{:keys [pin puk on-success on-failure]}]
  (when (and pin puk)
    (.. status-keycard
        (changePUK pin puk)
        (then on-success)
        (catch on-failure))))

(defn change-pairing
  [{:keys [pin pairing on-success on-failure]}]
  (when (and pin pairing)
    (.. status-keycard
        (changePairingPassword pin pairing)
        (then on-success)
        (catch on-failure))))

(defn unpair
  [{:keys [pin on-success on-failure]}]
  (when pin
    (.. status-keycard
        (unpair pin)
        (then on-success)
        (catch on-failure))))

(defn delete
  [{:keys [on-success on-failure]}]
  (.. status-keycard
      (delete)
      (then on-success)
      (catch on-failure)))

(defn remove-key
  [{:keys [pin on-success on-failure]}]
  (.. status-keycard
      (removeKey pin)
      (then on-success)
      (catch on-failure)))

(defn remove-key-with-unpair
  [{:keys [pin on-success on-failure]}]
  (.. status-keycard
      (removeKeyWithUnpair pin)
      (then on-success)
      (catch on-failure)))

(defn export-key
  [{:keys [pin path on-success on-failure]}]
  (.. status-keycard
      (exportKeyWithPath pin path)
      (then on-success)
      (catch on-failure)))

(defn unpair-and-delete
  [{:keys [pin on-success on-failure]}]
  (when (not-empty pin)
    (.. status-keycard
        (unpairAndDelete pin)
        (then on-success)
        (catch on-failure))))

(defn import-keys
  [{:keys [pin on-success on-failure]}]
  (when (not-empty pin)
    (.. status-keycard
        (importKeys pin)
        (then on-success)
        (catch on-failure))))

(defn get-keys
  [{:keys [pin on-success on-failure]}]
  (when (not-empty pin)
    (.. status-keycard
        (getKeys pin)
        (then on-success)
        (catch on-failure))))

(defn sign
  [{pin :pin path :path card-hash :hash on-success :on-success on-failure :on-failure}]
  (when (and pin card-hash)
    (if path
      (.. status-keycard
          (signWithPath pin path card-hash)
          (then on-success)
          (catch on-failure))
      (.. status-keycard
          (sign pin card-hash)
          (then on-success)
          (catch on-failure)))))

(defn sign-typed-data
  [{card-hash :hash on-success :on-success on-failure :on-failure}]
  (when card-hash
    (.. status-keycard
        (signPinless card-hash)
        (then on-success)
        (catch on-failure))))
