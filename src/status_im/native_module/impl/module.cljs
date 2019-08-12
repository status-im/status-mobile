(ns status-im.native-module.impl.module
  (:require [status-im.ui.components.react :as r]
            [re-frame.core :as re-frame]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [clojure.string :as string]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]))

(defn status []
  (when (exists? (.-NativeModules rn-dependencies/react-native))
    (.-Status (.-NativeModules rn-dependencies/react-native))))

(defonce listener-initialized (atom false))

(when-not @listener-initialized
  (reset! listener-initialized true)
  (.addListener r/device-event-emitter "gethEvent"
                #(re-frame/dispatch [:signals/signal-received (.-jsonEvent %)])))

(defonce node-started (atom false))

(defn stop-node []
  (reset! node-started false)
  (when (status)
    (.stopNode (status))))

(defn node-ready []
  (reset! node-started true))

(defn start-node [config]
  (when (status)
    (.startNode (status) config)))

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
                   (.createAccount (status) password callback)
                   true)
                 false))))))

(defn send-data-notification [{:keys [data-payload tokens] :as m} on-result]
  (when (status)
    (.sendDataNotification (status) data-payload tokens on-result)))

(defn send-logs [dbJson js-logs callback]
  (when (status)
    (.sendLogs (status) dbJson js-logs callback)))

(defn add-peer [enode on-result]
  (when (and @node-started (status))
    (.addPeer (status) enode on-result)))

(defn recover-account [passphrase password on-result]
  (when (and @node-started (status))
    (.recoverAccount (status) passphrase password on-result)))

(defn multiaccount-generate-and-derive-addresses [n mnemonic-length paths on-result]
  (when (and @node-started (status))
    (.multiAccountGenerateAndDeriveAddresses (status)
                                             (types/clj->json {:n n
                                                               :mnemonicPhraseLength mnemonic-length
                                                               :bip39Passphrase ""
                                                               :paths paths})
                                             on-result)))

(defn multiaccount-derive-addresses [account-id paths on-result]
  (when (and @node-started (status))
    (.multiAccountDeriveAddresses (status)
                                  (types/clj->json {:accountID account-id
                                                    :paths paths})
                                  on-result)))

(defn multiaccount-store-account [account-id password on-result]
  (when (and @node-started (status))
    (.multiAccountStoreAccount (status)
                               (types/clj->json {:accountID account-id
                                                 :password password})
                               on-result)))

(defn multiaccount-load-account [address password on-result]
  (when (and @node-started (status))
    (.multiAccountLoadAccount (status)
                              (types/clj->json {:address address
                                                :password password})
                              on-result)))

(defn multiaccount-reset [on-result]
  (when (and @node-started (status))
    (.multiAccountReset (status)
                        on-result)))

(defn multiaccount-store-derived [account-id paths password on-result]
  (when (and @node-started (status))
    (.multiAccountStoreDerived (status)
                               (types/clj->json {:accountID account-id
                                                 :paths paths
                                                 :password password})

                               on-result)))

(defn login [address password main-account watch-addresses on-result]
  (when (and @node-started (status))
    (.login (status)
            (types/clj->json {:chatAddress address :password password
                              :mainAccount main-account :watch-addresses watch-addresses})
            on-result)))

(defn verify [address password on-result]
  (when (and @node-started (status))
    (.verify (status) address password on-result)))

(defn login-with-keycard [whisper-private-key encryption-public-key on-result]
  (when (and @node-started (status))
    (.loginWithKeycard (status) whisper-private-key encryption-public-key on-result)))

(defn set-soft-input-mode [mode]
  (when (status)
    (.setSoftInputMode (status) mode)))

(defn clear-web-data []
  (when (status)
    (.clearCookies (status))
    (.clearStorageAPIs (status))))

(defn call-rpc [payload callback]
  (when (and @node-started (status))
    (.callRPC (status) payload callback)))

(defn call-private-rpc [payload callback]
  (when (and @node-started (status))
    (.callPrivateRPC (status) payload callback)))

(defn sign-message [rpcParams callback]
  (when (and @node-started (status))
    (.signMessage (status) rpcParams callback)))

(defn hash-transaction [rpcParams callback]
  (when (and @node-started (status))
    (.hashTransaction (status) rpcParams callback)))

(defn hash-message [message callback]
  (when (and @node-started (status))
    (.hashMessage (status) message callback)))

(defn hash-typed-data [data callback]
  (when (and @node-started (status))
    (.hashTypedData (status) data callback)))

(defn sign-typed-data [data account password callback]
  (when (and @node-started (status))
    (.signTypedData (status) data account password callback)))

(defn send-transaction [rpcParams password callback]
  (when (and @node-started (status))
    (.sendTransaction (status) rpcParams password callback)))

(defn send-transaction-with-signature [rpcParams sig callback]
  (when (and @node-started (status))
    (.sendTransactionWithSignature (status) rpcParams sig callback)))

(defn close-application []
  (.closeApplication (status)))

(defn connection-change [{:keys [type expensive?]}]
  (.connectionChange (status) type expensive?))

(defn app-state-change [state]
  (.appStateChange (status) state))

(defn get-device-UUID [callback]
  (.getDeviceUUID
   (status)
   (fn [UUID]
     (callback (string/upper-case UUID)))))

(defn set-blank-preview-flag [flag]
  (.setBlankPreviewFlag (status) flag))

(defn extract-group-membership-signatures [signature-pairs callback]
  (when (status)
    (.extractGroupMembershipSignatures (status) signature-pairs callback)))

(defn sign-group-membership [content callback]
  (when (status)
    (.signGroupMembership (status) content callback)))

(defn is24Hour []
  (when (status)
    (.-is24Hour (status))))

(defn get-device-model-info []
  (when status
    {:model     (.-model status)
     :brand     (.-brand status)
     :build-id  (.-buildId status)
     :device-id (.-deviceId status)}))

(defn update-mailservers [enodes on-result]
  (when (status)
    (.updateMailservers (status) enodes on-result)))

(defn chaos-mode-update [on on-result]
  (when (status)
    (.chaosModeUpdate (status) on on-result)))

(defn get-nodes-from-contract [rpc-endpoint contract-address on-result]
  (when (status)
    (.getNodesFromContract (status) rpc-endpoint contract-address on-result)))

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
