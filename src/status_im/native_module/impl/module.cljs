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
            [clojure.string :as string]))

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
                #(re-frame/dispatch [:signal-event (.-jsonEvent %)])))

(defn stop-node []
  (when status
    (call-module #(.stopNode status))))

(defn start-node [config fleet]
  (when status
    (call-module #(.startNode status config fleet))))

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

(defn notify-users [{:keys [message payload tokens] :as m} on-result]
  (when status
    (call-module #(.notifyUsers status message payload tokens on-result))))

(defn add-peer [enode on-result]
  (when status
    (call-module #(.addPeer status enode on-result))))

(defn recover-account [passphrase password on-result]
  (when status
    (call-module #(.recoverAccount status passphrase password on-result))))

(defn login [address password on-result]
  (when status
    (call-module #(.login status address password on-result))))

(defn set-soft-input-mode [mode]
  (when status
    (call-module #(.setSoftInputMode status mode))))

(defn clear-web-data []
  (when status
    (call-module #(.clearCookies status))
    (call-module #(.clearStorageAPIs status))))

(defn call-rpc [payload callback]
  (when status
    (call-module #(.callRPC status payload callback))))

(defn call-private-rpc [payload callback]
  (when status
    (call-module #(.callPrivateRPC status payload callback))))

(defn sign-message [rpcParams callback]
  (when status
    (call-module #(.signMessage status rpcParams callback))))

(defn send-transaction [rpcParams password callback]
  (when status
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

(defn is24Hour []
  (when status
    (.-is24Hour status)))
