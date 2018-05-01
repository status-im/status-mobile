(ns status-im.native-module.core
  (:require [status-im.native-module.module :as module-interface]
            [status-im.native-module.impl.module :as native-module]
            [status-im.native-module.impl.non-status-go-module :as non-status-go-module]
            [taoensso.timbre :as log]
            [status-im.utils.config :as config]))

(def rns-module
  (if config/stub-status-go?
    (non-status-go-module/ReactNativeStatus.)
    (native-module/ReactNativeStatus.)))


(def adjust-resize 16)
(def adjust-pan 32)

#_(defn- wrap-and-print-callback [name callback]
   (fn [& args]
     (println :callback name (str args))
     (log/debug :callback name args)
     (apply callback args)))

(defn init-jail []
  (module-interface/-init-jail rns-module))


(defn move-to-internal-storage [callback]
  (module-interface/-move-to-internal-storage rns-module callback))


(defn start-node [config]
  (module-interface/-start-node rns-module config))


(defn stop-node []
  (module-interface/-stop-node rns-module))


(defn create-account [password callback]
  (module-interface/-create-account rns-module password callback))


(defn recover-account [passphrase password callback]
  (module-interface/-recover-account rns-module passphrase password callback))


(defn login [address password callback]
  (module-interface/-login rns-module address password callback))


(defn approve-sign-requests [hashes password callback]
  (module-interface/-approve-sign-requests rns-module hashes password callback))


(defn discard-sign-request [id]
  (module-interface/-discard-sign-request rns-module id))

(defn parse-jail [chat-id file callback]
  (module-interface/-parse-jail rns-module chat-id file callback))


(defn call-jail [params]
  (module-interface/-call-jail rns-module params))


(defn call-function! [params]
  (module-interface/-call-function! rns-module params))


(defn set-soft-input-mode [mode]
  (module-interface/-set-soft-input-mode rns-module mode))


(defn clear-web-data []
  (module-interface/-clear-web-data rns-module))

(defn call-web3 [payload callback]
  (module-interface/-call-web3 rns-module payload callback))

(defn call-web3-private [payload callback]
  (module-interface/-call-web3-private rns-module payload callback))

(defn module-initialized! []
  (module-interface/-module-initialized! rns-module))

(defn should-move-to-internal-storage? [callback]
  (module-interface/-should-move-to-internal-storage? rns-module callback))

(defn notify-users [{:keys [message payload tokens] :as m} callback]
  (module-interface/-notify-users rns-module m callback))

(defn add-peer [enode callback]
  (module-interface/-add-peer rns-module enode callback))

(defn close-application []
  (module-interface/-close-application rns-module))

(defn connection-change [data]
  (module-interface/-connection-change rns-module data))

(defn app-state-change [state]
  (module-interface/-app-state-change rns-module state))

(defn get-device-UUID [callback]
  (module-interface/-get-device-UUID rns-module callback))
