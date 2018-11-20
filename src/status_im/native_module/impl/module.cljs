(ns status-im.native-module.impl.module
  (:require-macros
   [cljs.core.async.macros :refer [go-loop go]])
  (:require [status-im.ui.components.react :as r]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [cljs.core.async :as async]
            [status-im.utils.platform :as p]
            [status-im.utils.async :as async-util]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [clojure.string :as string]
            [status-im.utils.platform :as platform]))

;; if StatusModule is not initialized better to store
;; calls and make them only when StatusModule is ready
;; this flag helps to handle this
(defonce module-initialized? (atom (or p/ios? js/goog.DEBUG p/desktop?)))

;; array of calls to StatusModule
(defonce calls (atom []))

(defn module-initialized! []
  (reset! module-initialized? true))

(defn store-call [args]
  (log/debug :store-call args)
  (swap! calls conj args))

(defn call-module [f]
  ;;(log/debug :call-module f)
  (if @module-initialized?
    (f)
    (store-call f)))

(defonce loop-started (atom false))

(when-not @loop-started
  (go-loop [_ nil]
    (reset! loop-started true)
    (if (and (seq @calls) @module-initialized?)
      (do (swap! calls (fn [calls]
                         (doseq [call calls]
                           (call))))
          (reset! loop-started false))
      (recur (async/<! (async-util/timeout 500))))))

(def status
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
  (when status
    (call-module #(.stopNode status))))

(defn node-ready []
  (reset! node-started true))

(defn start-node [config]
  (when status
    (call-module #(.startNode status config))))

(defonce account-creation? (atom false))

(defn create-account [password on-result]
  (when status
    (let [callback (fn [data]
                     (reset! account-creation? false)
                     (on-result data))]
      (swap! account-creation?
             (fn [creation?]
               (if-not creation?
                 (do
                   (call-module #(.createAccount status password callback))
                   true)
                 false))))))

(defn notify-users [{:keys [data-payload tokens] :as m} on-result]
  (when status
    (call-module #(.notifyUsers status data-payload tokens on-result))))

(defn send-logs [dbJson]
  (when status
    (call-module #(.sendLogs status dbJson))))

(defn add-peer [enode on-result]
  (when (and @node-started status)
    (call-module #(.addPeer status enode on-result))))

(defn recover-account [passphrase password on-result]
  (when (and @node-started status)
    (call-module #(.recoverAccount status passphrase password on-result))))

(defn login [address password on-result]
  (when (and @node-started status)
    (call-module #(.login status address password on-result))))

(defn verify [address password on-result]
  (when (and @node-started status)
    (call-module #(.verify status address password on-result))))

(defn set-soft-input-mode [mode]
  (when status
    (call-module #(.setSoftInputMode status mode))))

(defn clear-web-data []
  (when status
    (call-module #(.clearCookies status))
    (call-module #(.clearStorageAPIs status))))

(defn call-rpc [payload callback]
  (when (and @node-started status)
    (call-module #(.callRPC status payload callback))))

(defn call-private-rpc [payload callback]
  (when (and @node-started status)
    (call-module #(.callPrivateRPC status payload callback))))

(defn sign-message [rpcParams callback]
  (when (and @node-started status)
    (call-module #(.signMessage status rpcParams callback))))

(defn send-transaction [rpcParams password callback]
  (when (and @node-started status)
    (call-module #(.sendTransaction status rpcParams password callback))))

(defn close-application []
  (.closeApplication status))

(defn connection-change [{:keys [type expensive?]}]
  (.connectionChange status type expensive?))

(defn app-state-change [state]
  (.appStateChange status state))

(defn get-device-UUID [callback]
  (call-module
   #(.getDeviceUUID
     status
     (fn [UUID]
       (callback (string/upper-case UUID))))))

(defn extract-group-membership-signatures [signature-pairs callback]
  (when status
    (call-module #(.extractGroupMembershipSignatures status signature-pairs callback))))

(defn sign-group-membership [content callback]
  (when status
    (call-module #(.signGroupMembership status content callback))))

(defn enable-installation [installation-id callback]
  (when status
    (call-module #(.enableInstallation status installation-id callback))))

(defn disable-installation [installation-id callback]
  (when status
    (call-module #(.disableInstallation status installation-id callback))))

(defn is24Hour []
  (when status
    (.-is24Hour status)))

(defn update-mailservers [enodes on-result]
  (when status
    (call-module #(.updateMailservers status enodes on-result))))

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
    (if status
      (call-module #(.isDeviceRooted status callback))
      ;; if module isn't initialized we return true to avoid degrading security
      (callback true))

    ;; in unknown scenarios we also consider the device rooted to avoid degrading security
    :else (callback true)))
