(ns status-im.keycard.real-keycard
  (:require ["react-native" :as rn]
            ["react-native-status-keycard" :default status-keycard]
            [clojure.string :as string]
            [status-im.ethereum.core :as ethereum]
            [status-im.keycard.keycard :as keycard]
            [native-module.core :as native-module]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(defonce event-emitter
  (if platform/ios?
    (new (.-NativeEventEmitter rn) status-keycard)
    (.-DeviceEventEmitter rn)))

(defonce active-listeners (atom []))

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
  (.remove event))

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
  [{:keys [pairings]}]
  (.. status-keycard (setPairings (clj->js (or pairings {})))))

(defn register-card-events
  [args]
  (doseq [listener @active-listeners]
    (remove-event-listener listener))
  (reset! active-listeners
    [(on-card-connected (:on-card-connected args))
     (on-card-disconnected (:on-card-disconnected args))
     (on-nfc-user-cancelled (:on-nfc-user-cancelled args))
     (on-nfc-timeout (:on-nfc-timeout args))
     (on-nfc-enabled (:on-nfc-enabled args))
     (on-nfc-disabled (:on-nfc-disabled args))]))

(defn get-application-info
  [{:keys [on-success on-failure]}]

  (.. status-keycard
      (getApplicationInfo)
      (then (fn [response]
              (let [info (-> response
                             (js->clj :keywordize-keys true)
                             (update :key-uid ethereum/normalized-hex))]
                (on-success info))))
      (catch on-failure)))

(defn factory-reset
  [{:keys [on-success on-failure]}]
  (.. status-keycard
      (factoryReset)
      (then (fn [response]
              (let [info (-> response
                             (js->clj :keywordize-keys true)
                             (update :key-uid ethereum/normalized-hex))]
                (on-success info))))
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
  (log/debug "keycard sign" "path" path)
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

(defn save-multiaccount-and-login
  [{:keys [key-uid multiaccount-data password settings node-config accounts-data chat-key]}]
  (native-module/save-multiaccount-and-login-with-keycard
   key-uid
   (types/clj->json multiaccount-data)
   password
   (types/clj->json settings)
   node-config
   (types/clj->json accounts-data)
   chat-key))

(defn login
  [args]
  (native-module/login-with-keycard (assoc args :node-config {:ProcessBackedupMessages false})))

(defn send-transaction-with-signature
  [{:keys [transaction signature on-completed]}]
  (native-module/send-transaction-with-signature transaction signature on-completed))

(defn delete-multiaccount-before-migration
  [{:keys [key-uid on-success on-error]}]
  (native-module/delete-multiaccount
   key-uid
   (fn [result]
     (let [{:keys [error]} (types/json->clj result)]
       (if-not (string/blank? error)
         (on-error error)
         (on-success))))))

(defrecord RealKeycard []
  keycard/Keycard
    (keycard/start-nfc [_this args]
      (start-nfc args))
    (keycard/stop-nfc [_this args]
      (stop-nfc args))
    (keycard/set-nfc-message [_this args]
      (set-nfc-message args))
    (keycard/check-nfc-support [_this args]
      (check-nfc-support args))
    (keycard/check-nfc-enabled [_this args]
      (check-nfc-enabled args))
    (keycard/open-nfc-settings [_this]
      (open-nfc-settings))
    (keycard/register-card-events [_this args]
      (register-card-events args))
    (keycard/on-card-connected [_this callback]
      (on-card-connected callback))
    (keycard/on-card-disconnected [_this callback]
      (on-card-disconnected callback))
    (keycard/remove-event-listener [_this event]
      (remove-event-listener event))
    (keycard/remove-event-listeners [_this]
      (remove-event-listeners))
    (keycard/set-pairings [_this args]
      (set-pairings args))
    (keycard/get-application-info [_this args]
      (get-application-info args))
    (keycard/factory-reset [_this args]
      (factory-reset args))
    (keycard/install-applet [_this args]
      (install-applet args))
    (keycard/install-cash-applet [_this args]
      (install-cash-applet args))
    (keycard/init-card [_this args]
      (init-card args))
    (keycard/install-applet-and-init-card [_this args]
      (install-applet-and-init-card args))
    (keycard/pair [_this args]
      (pair args))
    (keycard/generate-and-load-key [_this args]
      (generate-and-load-key args))
    (keycard/unblock-pin [_this args]
      (unblock-pin args))
    (keycard/verify-pin [_this args]
      (verify-pin args))
    (keycard/change-pin [_this args]
      (change-pin args))
    (keycard/change-puk [_this args]
      (change-puk args))
    (keycard/change-pairing [_this args]
      (change-pairing args))
    (keycard/unpair [_this args]
      (unpair args))
    (keycard/delete [_this args]
      (delete args))
    (keycard/remove-key [_this args]
      (remove-key args))
    (keycard/remove-key-with-unpair [_this args]
      (remove-key-with-unpair args))
    (keycard/export-key [_this args]
      (export-key args))
    (keycard/unpair-and-delete [_this args]
      (unpair-and-delete args))
    (keycard/import-keys [_this args]
      (import-keys args))
    (keycard/get-keys [_this args]
      (get-keys args))
    (keycard/sign [_this args]
      (sign args))
    (keycard/sign-typed-data [_this args]
      (sign-typed-data args))
    (keycard/save-multiaccount-and-login [_this args]
      (save-multiaccount-and-login args))
    (keycard/login [_this args]
      (login args))
    (keycard/send-transaction-with-signature [_this args]
      (send-transaction-with-signature args))
    (keycard/delete-multiaccount-before-migration [_ args]
      (delete-multiaccount-before-migration args)))
