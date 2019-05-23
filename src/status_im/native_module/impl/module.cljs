(ns status-im.native-module.impl.module
  (:require [status-im.ui.components.react :as r]
            [re-frame.core :as re-frame]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [clojure.string :as string]
            [status-im.utils.platform :as platform]))

(defn status []
  (when (exists? (.-NativeModules ^js rn-dependencies/react-native))
    (.-Status (.-NativeModules ^js rn-dependencies/react-native))))

(defonce listener-initialized (atom false))

(when-not @listener-initialized
  (reset! listener-initialized true)
  (.addListener ^js r/device-event-emitter "gethEvent"
                #(re-frame/dispatch [:signals/signal-received (.-jsonEvent ^js %)])))

(defonce node-started (atom false))

(defn stop-node []
  (reset! node-started false)
  (when (status)
    (.stopNode ^js (status))))

(defn node-ready []
  (reset! node-started true))

(defn start-node [config]
  (when (status)
    (.startNode ^js (status) config)))

(defonce account-creation? (atom false))

(defn create-account [password on-result]
  (when (status)
    (let [callback (fn [data]
                     (reset! account-creation? false)
                     (on-result data))]
      (swap! account-creation?
             (fn [creation?]
               (if-not creation?
                 (do
                   (.createAccount ^js (status) password callback)
                   true)
                 false))))))

(defn send-data-notification [{:keys [data-payload tokens] :as m} on-result]
  (when (status)
    (.sendDataNotification ^js (status) data-payload tokens on-result)))

(defn send-logs [dbJson js-logs callback]
  (when (status)
    (.sendLogs ^js (status) dbJson js-logs callback)))

(defn add-peer [enode on-result]
  (when (and @node-started (status))
    (.addPeer ^js (status) enode on-result)))

(defn recover-account [passphrase password on-result]
  (when (and @node-started (status))
    (.recoverAccount ^js (status) passphrase password on-result)))

(defn login [address password on-result]
  (when (and @node-started (status))
    (.login ^js (status) address password on-result)))

(defn verify [address password on-result]
  (when (and @node-started (status))
    (.verify ^js (status) address password on-result)))

(defn login-with-keycard [whisper-private-key encryption-public-key on-result]
  (when (and @node-started (status))
    (.loginWithKeycard ^js (status) whisper-private-key encryption-public-key on-result)))

(defn set-soft-input-mode [mode]
  (when (status)
    (.setSoftInputMode ^js (status) mode)))

(defn clear-web-data []
  (when (status)
    (.clearCookies ^js (status))
    (.clearStorageAPIs ^js (status))))

(defn call-rpc [payload callback]
  (when (and @node-started (status))
    (.callRPC ^js (status) payload callback)))

(defn call-private-rpc [payload callback]
  (when (and @node-started (status))
    (.callPrivateRPC ^js (status) payload callback)))

(defn sign-message [rpcParams callback]
  (when (and @node-started (status))
    (.signMessage ^js (status) rpcParams callback)))

(defn hash-transaction [rpcParams callback]
  (when (and @node-started (status))
    (.hashTransaction ^js (status) rpcParams callback)))

(defn hash-message [message callback]
  (when (and @node-started (status))
    (.hashMessage ^js (status) message callback)))

(defn hash-typed-data [data callback]
  (when (and @node-started (status))
    (.hashTypedData ^js (status) data callback)))

(defn sign-typed-data [data password callback]
  (when (and @node-started (status))
    (.signTypedData ^js (status) data password callback)))

(defn send-transaction [rpcParams password callback]
  (when (and @node-started (status))
    (.sendTransaction ^js (status) rpcParams password callback)))

(defn send-transaction-with-signature [rpcParams sig callback]
  (when (and @node-started (status))
    (.sendTransactionWithSignature ^js (status) rpcParams sig callback)))

(defn close-application []
  (.closeApplication ^js (status)))

(defn connection-change [{:keys [type expensive?]}]
  (.connectionChange ^js (status) type expensive?))

(defn app-state-change [state]
  (.appStateChange ^js (status) state))

(defn get-device-UUID [callback]
  (.getDeviceUUID
   ^js (status)
   (fn [UUID]
     (callback (string/upper-case UUID)))))

(defn set-blank-preview-flag [flag]
  (.setBlankPreviewFlag ^js status flag))

(defn extract-group-membership-signatures [signature-pairs callback]
  (when (status)
    (.extractGroupMembershipSignatures ^js (status) signature-pairs callback)))

(defn sign-group-membership [content callback]
  (when (status)
    (.signGroupMembership ^js (status) content callback)))

(defn enable-installation [installation-id callback]
  (when (status)
    (.enableInstallation ^js (status) installation-id callback)))

(defn disable-installation [installation-id callback]
  (when (status)
    (.disableInstallation ^js (status) installation-id callback)))

(defn is24Hour []
  (when (status)
    (.-is24Hour ^js (status))))

(defn update-mailservers [enodes on-result]
  (when (status)
    (.updateMailservers ^js (status) enodes on-result)))

(defn chaos-mode-update [on on-result]
  (when (status)
    (.chaosModeUpdate ^js (status) on on-result)))

(defn get-nodes-from-contract [rpc-endpoint contract-address on-result]
  (when (status)
    (.getNodesFromContract ^js (status) rpc-endpoint contract-address on-result)))

(defn rooted-device? [callback]
  (cond
    ;; we assume that iOS is safe by default
    platform/ios?
    (callback false)

    ;; we assume that Desktop is unsafe by default
    ;; (theoretically, Desktop is always "rooted", by design
    platform/desktop?
    (callback true)

    ;; we check root on android
    platform/android?
    (if (status)
      (.isDeviceRooted (status) callback)
      ;; if module isn't initialized we return true to avoid degrading security
      (callback true))

    ;; in unknown scenarios we also consider the device rooted to avoid degrading security
    :else (callback true)))
