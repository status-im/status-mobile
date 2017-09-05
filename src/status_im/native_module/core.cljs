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


(defn start-node [callback]
  (module-interface/-start-node rns-module callback))


(defn stop-rpc-server []
  (module-interface/-stop-rpc-server rns-module))


(defn start-rpc-server []
  (module-interface/-start-rpc-server rns-module))


(defn restart-rpc []
  (module-interface/-restart-rpc rns-module))


(defn create-account [password callback]
  (module-interface/-create-account rns-module password callback))


(defn recover-account [passphrase password callback]
  (module-interface/-recover-account rns-module passphrase password callback))


(defn login [address password callback]
  (module-interface/-login rns-module address password callback))


(defn complete-transactions [hashes password callback]
  (module-interface/-complete-transactions rns-module hashes password callback))


(defn discard-transaction [id]
  (module-interface/-discard-transaction rns-module id))


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

(defn call-web3 [host payload callback]
  (module-interface/-call-web3 rns-module host payload callback))

(defn module-initialized! []
  (module-interface/-module-initialized! rns-module))

(defn should-move-to-internal-storage? [callback]
  (module-interface/-should-move-to-internal-storage? rns-module callback))
