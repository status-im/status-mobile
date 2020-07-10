(ns status-im.keycard.real-keycard
  (:require ["react-native-status-keycard" :default status-keycard]
            ["react-native" :as rn]
            [status-im.utils.types :as types]
            [status-im.native-module.core :as status]
            [status-im.ethereum.core :as ethereum]
            [status-im.keycard.keycard :as keycard]
            [taoensso.timbre :as log]))

(defonce event-emitter (.-DeviceEventEmitter rn))
(defonce active-listeners (atom []))

(defn check-nfc-support [{:keys [on-success]}]
  (.. status-keycard
      nfcIsSupported
      (then on-success)))

(defn check-nfc-enabled [{:keys [on-success]}]
  (.. status-keycard
      nfcIsEnabled
      (then on-success)))

(defn open-nfc-settings []
  (.openNfcSettings status-keycard))

(defn remove-event-listeners []
  (doseq [event ["keyCardOnConnected" "keyCardOnDisconnected"]]
    (.removeAllListeners ^js event-emitter event)))

(defn remove-event-listener
  [^js event]
  (.remove event))

(defn on-card-connected
  [callback]
  (.addListener ^js event-emitter "keyCardOnConnected" callback))

(defn on-card-disconnected
  [callback]
  (.addListener ^js event-emitter "keyCardOnDisconnected" callback))

(defn on-nfc-enabled
  [callback]
  (.addListener ^js event-emitter "keyCardOnNFCEnabled" callback))

(defn on-nfc-disabled
  [callback]
  (.addListener ^js event-emitter "keyCardOnNFCDisabled" callback))

(defn register-card-events
  [args]
  (doseq [listener @active-listeners]
    (remove-event-listener listener))
  (reset! active-listeners
          [(on-card-connected (:on-card-connected args))
           (on-card-disconnected (:on-card-disconnected args))
           (on-nfc-enabled (:on-nfc-enabled args))
           (on-nfc-disabled (:on-nfc-disabled args))]))

(defn get-application-info
  [{:keys [pairing on-success on-failure]}]
  ;; NOTE: if the card fails to get application info in the middle of the call
  ;; it doesn't returns a Tar was lost. error properly
  ;; https://github.com/status-im/react-native-status-keycard/blob/master/android/src/main/java/im/status/ethereum/keycard/SmartCard.java#L235

  (.. status-keycard
      (getApplicationInfo (str pairing))
      (then (fn [response]
              (let [info (-> response
                             (js->clj :keywordize-keys true)
                             (update :key-uid ethereum/normalized-hex))]
                (if (and pairing (nil? (:pin-retry-counter info)))
                  (on-failure (clj->js {:message "Tag was lost."
                                        :code "android.nfc.TagLostException"}))
                  (on-success info)))))
      (catch on-failure)))

(defn install-applet [{:keys [on-success on-failure]}]
  (.. status-keycard
      installApplet
      (then on-success)
      (catch on-failure)))

(defn install-cash-applet [{:keys [on-success on-failure]}]
  (.. status-keycard
      installCashApplet
      (then on-success)
      (catch on-failure)))

(defn init-card [{:keys [pin on-success on-failure]}]
  (.. status-keycard
      (init pin)
      (then on-success)
      (catch on-failure)))

(defn install-applet-and-init-card [{:keys [pin on-success on-failure]}]
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
  [{:keys [mnemonic pairing pin on-success on-failure]}]
  (when pairing
    (.. status-keycard
        (generateAndLoadKey mnemonic pairing pin)
        (then on-success)
        (catch on-failure))))

(defn unblock-pin
  [{:keys [puk new-pin pairing on-success on-failure]}]
  (when (and pairing new-pin puk)
    (.. status-keycard
        (unblockPin pairing puk new-pin)
        (then on-success)
        (catch on-failure))))

(defn verify-pin
  [{:keys [pin pairing on-success on-failure]}]
  (when (and pairing (not-empty pin))
    (.. status-keycard
        (verifyPin pairing pin)
        (then on-success)
        (catch on-failure))))

(defn change-pin
  [{:keys [current-pin new-pin pairing on-success on-failure]}]
  (when (and pairing current-pin new-pin)
    (.. status-keycard
        (changePin pairing current-pin new-pin)
        (then on-success)
        (catch on-failure))))

(defn unpair
  [{:keys [pin pairing on-success on-failure]}]
  (when (and pairing pin)
    (.. status-keycard
        (unpair pairing pin)
        (then on-success)
        (catch on-failure))))

(defn delete [{:keys [on-success on-failure]}]
  (.. status-keycard
      (delete)
      (then on-success)
      (catch on-failure)))

(defn remove-key
  [{:keys [pin pairing on-success on-failure]}]
  (.. status-keycard
      (removeKey pairing pin)
      (then on-success)
      (catch on-failure)))

(defn remove-key-with-unpair
  [{:keys [pin pairing on-success on-failure]}]
  (.. status-keycard
      (removeKeyWithUnpair pairing pin)
      (then on-success)
      (catch on-failure)))

(defn export-key
  [{:keys [pin pairing path on-success on-failure]}]
  (.. status-keycard
      (exportKeyWithPath pairing pin path)
      (then on-success)
      (catch on-failure)))

(defn unpair-and-delete
  [{:keys [pin pairing on-success on-failure]}]
  (when (and pairing pin)
    (.. status-keycard
        (unpairAndDelete pairing pin)
        (then on-success)
        (catch on-failure))))

(defn get-keys
  [{:keys [pairing pin on-success on-failure]}]
  (when (and pairing (not-empty pin))
    (.. status-keycard
        (getKeys pairing pin)
        (then on-success)
        (catch on-failure))))

(defn sign
  [{:keys [pairing pin path hash on-success on-failure]}]
  (log/debug "keycard sign" "path" path)
  (when (and pairing pin hash)
    (if path
      (.. status-keycard
          (signWithPath pairing pin path hash)
          (then on-success)
          (catch on-failure))
      (.. status-keycard
          (sign pairing pin hash)
          (then on-success)
          (catch on-failure)))))

(defn sign-typed-data
  [{:keys [hash on-success on-failure]}]
  (when hash
    (.. status-keycard
        (signPinless hash)
        (then on-success)
        (catch on-failure))))

(defn save-multiaccount-and-login
  [{:keys [key-uid multiaccount-data password settings node-config accounts-data chat-key]}]
  (status/save-multiaccount-and-login-with-keycard
   key-uid
   (types/clj->json multiaccount-data)
   password
   (types/clj->json settings)
   node-config
   (types/clj->json accounts-data)
   chat-key))

(defn login [args]
  (status/login-with-keycard args))

(defn send-transaction-with-signature
  [{:keys [transaction signature on-completed]}]
  (status/send-transaction-with-signature transaction signature on-completed))

(defrecord RealKeycard []
  keycard/Keycard
  (keycard/check-nfc-support [this args]
    (check-nfc-support args))
  (keycard/check-nfc-enabled [this args]
    (check-nfc-enabled args))
  (keycard/open-nfc-settings [this]
    (open-nfc-settings))
  (keycard/register-card-events [this args]
    (register-card-events args))
  (keycard/on-card-connected [this callback]
    (on-card-connected callback))
  (keycard/on-card-disconnected [this callback]
    (on-card-disconnected callback))
  (keycard/remove-event-listener [this event]
    (remove-event-listener event))
  (keycard/remove-event-listeners [this]
    (remove-event-listeners))
  (keycard/get-application-info [this args]
    (get-application-info args))
  (keycard/install-applet [this args]
    (install-applet args))
  (keycard/install-cash-applet [this args]
    (install-cash-applet args))
  (keycard/init-card [this args]
    (init-card args))
  (keycard/install-applet-and-init-card [this args]
    (install-applet-and-init-card args))
  (keycard/pair [this args]
    (pair args))
  (keycard/generate-and-load-key [this args]
    (generate-and-load-key args))
  (keycard/unblock-pin [this args]
    (unblock-pin args))
  (keycard/verify-pin [this args]
    (verify-pin args))
  (keycard/change-pin [this args]
    (change-pin args))
  (keycard/unpair [this args]
    (unpair args))
  (keycard/delete [this args]
    (delete args))
  (keycard/remove-key [this args]
    (remove-key args))
  (keycard/remove-key-with-unpair [this args]
    (remove-key-with-unpair args))
  (keycard/export-key [this args]
    (export-key args))
  (keycard/unpair-and-delete [this args]
    (unpair-and-delete args))
  (keycard/get-keys [this args]
    (get-keys args))
  (keycard/sign [this args]
    (sign args))
  (keycard/sign-typed-data [this args]
    (sign-typed-data args))
  (keycard/save-multiaccount-and-login [this args]
    (save-multiaccount-and-login args))
  (keycard/login [this args]
    (login args))
  (keycard/send-transaction-with-signature [this args]
    (send-transaction-with-signature args)))
